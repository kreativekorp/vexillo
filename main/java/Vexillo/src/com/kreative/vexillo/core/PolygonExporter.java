package com.kreative.vexillo.core;

import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PolygonExporter {
	public static String toCIntArrayString(Map<String, Polygon> polygons) {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, Polygon> e : polygons.entrySet()) {
			if (sb.length() > 0) sb.append(" ");
			sb.append("int ");
			sb.append(e.getKey());
			sb.append("[] = {");
			for (int i = 0; i < e.getValue().npoints; i++) {
				sb.append(e.getValue().xpoints[i]);
				sb.append(",");
				sb.append(e.getValue().ypoints[i]);
				sb.append(",");
			}
			sb.append("-1,-1};");
		}
		return sb.toString();
	}
	
	private final File flagFile;
	private final File parent;
	private final Flag flag;
	
	public PolygonExporter(File flagFile, File parent, Flag flag) {
		this.flagFile = flagFile;
		this.parent = parent;
		this.flag = flag;
	}
	
	public File getFlagFile() { return flagFile; }
	public File getParentFile() { return parent; }
	public Flag getFlag() { return flag; }
	
	public Map<String, Polygon> getPolygons(int w, int h, Map<String, Polygon> polygons, double flatness) {
		if (polygons == null) polygons = new LinkedHashMap<String, Polygon>();
		Map<String, Dimension> d = flag.createNamespace(h, w);
		for (Instruction i : flag.instructions()) execute(i, d, polygons, flatness);
		return polygons;
	}
	
	private void execute(Instruction i, Map<String, Dimension> d, Map<String, Polygon> polygons, double flatness) {
		if (i instanceof Instruction.GroupInstruction) {
			Instruction.GroupInstruction gi = (Instruction.GroupInstruction)i;
			for (Instruction j : gi.clippingRegion) execute(j, d, polygons, flatness);
			for (Instruction j : gi.instructions) execute(j, d, polygons, flatness);
		} else if (i instanceof Instruction.ForInstruction) {
			Instruction.ForInstruction fi = (Instruction.ForInstruction)i;
			double start = fi.start.value(d);
			double end = fi.end.value(d);
			double step = fi.step.value(d);
			while (start <= end) {
				d.put(fi.var, new Dimension.Constant(start));
				for (Instruction j : fi.instructions) execute(j, d, polygons, flatness);
				start += step;
			}
		} else if (i instanceof Instruction.PolyInstruction) {
			Instruction.PolyInstruction pi = (Instruction.PolyInstruction)i;
			int[] xCoords = new int[pi.points];
			int[] yCoords = new int[pi.points];
			for (int idx = 0; idx < pi.points; idx++) {
				xCoords[idx] = (int)Math.round(pi.x[idx].value(d));
				yCoords[idx] = (int)Math.round(pi.y[idx].value(d));
			}
			int n = 0; while (polygons.containsKey("polygon" + n)) n++;
			polygons.put("polygon" + n, new Polygon(xCoords, yCoords, pi.points));
		} else if (i instanceof Instruction.SymbolInstruction) {
			// Get symbol shape
			Instruction.SymbolInstruction si = (Instruction.SymbolInstruction)i;
			Symbol s = flag.symbols().get(si.symbolName); if (s == null) return;
			Shape sh = s.toPath(); if (sh == null) return;
			// Transform shape
			double shr = si.rotate.value(d);
			if (shr != 0) sh = AffineTransform.getRotateInstance(Math.toRadians(shr)).createTransformedShape(sh);
			double shsx = si.sx.value(d); double shsy = si.sy.value(d);
			sh = AffineTransform.getScaleInstance(shsx, shsy).createTransformedShape(sh);
			double shx = si.x.value(d); double shy = si.y.value(d);
			sh = AffineTransform.getTranslateInstance(shx, shy).createTransformedShape(sh);
			// Convert to polygons
			int n = 0; while (polygons.containsKey(si.symbolName + n)) n++;
			final LinkedList<Point2D.Float> currentPoly = new LinkedList<Point2D.Float>();
			final float[] coords = new float[6];
			for (PathIterator pi = sh.getPathIterator(null, flatness); !pi.isDone(); pi.next()) {
				switch (pi.currentSegment(coords)) {
					case PathIterator.SEG_MOVETO:
						Point2D.Float p0 = new Point2D.Float(coords[0], coords[1]);
						if (currentPoly.size() > 0) {
							if (currentPoly.size() > 1) {
								polygons.put(si.symbolName + (n++), pointsToPolygon(currentPoly));
							}
							currentPoly.clear();
						}
						currentPoly.add(p0);
						break;
					case PathIterator.SEG_LINETO:
						Point2D.Float p1 = new Point2D.Float(coords[0], coords[1]);
						if (currentPoly.size() > 0 && p1.equals(currentPoly.getFirst())) {
							if (currentPoly.size() > 1) {
								polygons.put(si.symbolName + (n++), pointsToPolygon(currentPoly));
							}
							currentPoly.clear();
						}
						currentPoly.add(p1);
						break;
					case PathIterator.SEG_QUADTO:
						Point2D.Float p2 = new Point2D.Float(coords[2], coords[3]);
						if (currentPoly.size() > 0 && p2.equals(currentPoly.getFirst())) {
							if (currentPoly.size() > 1) {
								polygons.put(si.symbolName + (n++), pointsToPolygon(currentPoly));
							}
							currentPoly.clear();
						}
						currentPoly.add(p2);
						break;
					case PathIterator.SEG_CUBICTO:
						Point2D.Float p3 = new Point2D.Float(coords[4], coords[5]);
						if (currentPoly.size() > 0 && p3.equals(currentPoly.getFirst())) {
							if (currentPoly.size() > 1) {
								polygons.put(si.symbolName + (n++), pointsToPolygon(currentPoly));
							}
							currentPoly.clear();
						}
						currentPoly.add(p3);
						break;
					case PathIterator.SEG_CLOSE:
						if (currentPoly.size() > 0) {
							Point2D.Float p4 = currentPoly.getFirst();
							if (currentPoly.size() > 1) {
								polygons.put(si.symbolName + (n++), pointsToPolygon(currentPoly));
							}
							currentPoly.clear();
							currentPoly.add(p4);
						}
						break;
				}
			}
			if (currentPoly.size() > 1) {
				polygons.put(si.symbolName + n, pointsToPolygon(currentPoly));
			}
		}
	}
	
	private static Polygon pointsToPolygon(List<? extends Point2D> polygon) {
		int index = 0;
		int ncoords = polygon.size();
		int[] xcoords = new int[ncoords];
		int[] ycoords = new int[ncoords];
		for (Point2D point : polygon) {
			int x = (int)Math.round(point.getX());
			int y = (int)Math.round(point.getY());
			if (index == 0 || xcoords[index - 1] != x || ycoords[index - 1] != y) {
				xcoords[index] = x;
				ycoords[index] = y;
				index++;
			}
		}
		return new Polygon(xcoords, ycoords, index);
	}
}