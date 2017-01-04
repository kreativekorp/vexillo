package com.kreative.vexillo.core;

import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Symbol {
	private final GeneralPath path;
	private final String normalizedString;
	
	public Symbol(String s) {
		this.path = new GeneralPath();
		this.normalizedString = parseInstructions(s, this.path);
	}
	
	public GeneralPath toPath() {
		return this.path;
	}
	
	@Override
	public String toString() {
		return this.normalizedString;
	}
	
	private static final Pattern INSTRUCTION_PATTERN = Pattern.compile("[A-Za-z]|([+-]?)([0-9]+([.][0-9]*)?|[.][0-9]+)");
	private static String parseInstructions(String s, GeneralPath p) {
		List<String> instructions = new ArrayList<String>();
		float lcx = 0.0f, lcy = 0.0f, lx = 0.0f, ly = 0.0f;
		float ccx, ccy, arx, ary, aa;
		boolean large, sweep;
		float rx, ry, rw, rh, rrx, rry, ras, rae;
		int rat;
		Matcher m = INSTRUCTION_PATTERN.matcher(s);
		while (m.find()) {
			String inst = m.group();
			switch (inst.charAt(0)) {
				case 'M':
					instructions.add("M");
					instructions.add(Float.toString(lcx = lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly = parseInstructionFloat(m)));
					p.moveTo(lx, ly);
					break;
				case 'm':
					instructions.add("M");
					instructions.add(Float.toString(lcx = lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly += parseInstructionFloat(m)));
					p.moveTo(lx, ly);
					break;
				case 'H':
					instructions.add("H");
					instructions.add(Float.toString(lcx = lx = parseInstructionFloat(m)));
					p.lineTo(lx, lcy = ly);
					break;
				case 'h':
					instructions.add("H");
					instructions.add(Float.toString(lcx = lx += parseInstructionFloat(m)));
					p.lineTo(lx, lcy = ly);
					break;
				case 'V':
					instructions.add("V");
					instructions.add(Float.toString(lcy = ly = parseInstructionFloat(m)));
					p.lineTo(lcx = lx, ly);
					break;
				case 'v':
					instructions.add("V");
					instructions.add(Float.toString(lcy = ly += parseInstructionFloat(m)));
					p.lineTo(lcx = lx, ly);
					break;
				case 'L':
					instructions.add("L");
					instructions.add(Float.toString(lcx = lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly = parseInstructionFloat(m)));
					p.lineTo(lx, ly);
					break;
				case 'l':
					instructions.add("L");
					instructions.add(Float.toString(lcx = lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly += parseInstructionFloat(m)));
					p.lineTo(lx, ly);
					break;
				case 'Q':
					instructions.add("Q");
					instructions.add(Float.toString(lcx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = parseInstructionFloat(m)));
					instructions.add(Float.toString(lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ly = parseInstructionFloat(m)));
					p.quadTo(lcx, lcy, lx, ly);
					break;
				case 'q':
					instructions.add("Q");
					instructions.add(Float.toString(lcx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(ly += parseInstructionFloat(m)));
					p.quadTo(lcx, lcy, lx, ly);
					break;
				case 'T':
					instructions.add("T");
					lcx = lx + lx - lcx;
					lcy = ly + ly - lcy;
					instructions.add(Float.toString(lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ly = parseInstructionFloat(m)));
					p.quadTo(lcx, lcy, lx, ly);
					break;
				case 't':
					instructions.add("T");
					lcx = lx + lx - lcx;
					lcy = ly + ly - lcy;
					instructions.add(Float.toString(lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(ly += parseInstructionFloat(m)));
					p.quadTo(lcx, lcy, lx, ly);
					break;
				case 'C':
					instructions.add("C");
					instructions.add(Float.toString(ccx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ccy = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = parseInstructionFloat(m)));
					instructions.add(Float.toString(lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ly = parseInstructionFloat(m)));
					p.curveTo(ccx, ccy, lcx, lcy, lx, ly);
					break;
				case 'c':
					instructions.add("C");
					instructions.add(Float.toString(ccx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(ccy = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(lcx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(ly += parseInstructionFloat(m)));
					p.curveTo(ccx, ccy, lcx, lcy, lx, ly);
					break;
				case 'S':
					instructions.add("S");
					ccx = lx + lx - lcx;
					ccy = ly + ly - lcy;
					instructions.add(Float.toString(lcx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = parseInstructionFloat(m)));
					instructions.add(Float.toString(lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ly = parseInstructionFloat(m)));
					p.curveTo(ccx, ccy, lcx, lcy, lx, ly);
					break;
				case 's':
					instructions.add("S");
					ccx = lx + lx - lcx;
					ccy = ly + ly - lcy;
					instructions.add(Float.toString(lcx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(ly += parseInstructionFloat(m)));
					p.curveTo(ccx, ccy, lcx, lcy, lx, ly);
					break;
				case 'A':
					instructions.add("A");
					instructions.add(Float.toString(arx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ary = parseInstructionFloat(m)));
					instructions.add(Float.toString(aa = parseInstructionFloat(m)));
					instructions.add((large = (parseInstructionFloat(m) != 0)) ? "1" : "0");
					instructions.add((sweep = (parseInstructionFloat(m) != 0)) ? "1" : "0");
					instructions.add(Float.toString(lcx = lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly = parseInstructionFloat(m)));
					arcTo(p, arx, ary, aa, large, sweep, lx, ly);
					break;
				case 'a':
					instructions.add("A");
					instructions.add(Float.toString(arx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ary = parseInstructionFloat(m)));
					instructions.add(Float.toString(aa = parseInstructionFloat(m)));
					instructions.add((large = (parseInstructionFloat(m) != 0)) ? "1" : "0");
					instructions.add((sweep = (parseInstructionFloat(m) != 0)) ? "1" : "0");
					instructions.add(Float.toString(lcx = lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly += parseInstructionFloat(m)));
					arcTo(p, arx, ary, aa, large, sweep, lx, ly);
					break;
				case 'Z':
				case 'z':
					instructions.add("Z");
					p.closePath();
					lcx = lx = (float)p.getCurrentPoint().getX();
					lcy = ly = (float)p.getCurrentPoint().getY();
					break;
				case 'G':
					instructions.add("G");
					instructions.add(Float.toString(ccx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ccy = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcx = lx = parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly = parseInstructionFloat(m)));
					coArcTo(p, ccx, ccy, lx, ly);
					break;
				case 'g':
					instructions.add("G");
					instructions.add(Float.toString(ccx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(ccy = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(lcx = lx += parseInstructionFloat(m)));
					instructions.add(Float.toString(lcy = ly += parseInstructionFloat(m)));
					coArcTo(p, ccx, ccy, lx, ly);
					break;
				case 'R':
					instructions.add("R");
					instructions.add(Float.toString(rx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ry = parseInstructionFloat(m)));
					instructions.add(Float.toString(rw = parseInstructionFloat(m)));
					instructions.add(Float.toString(rh = parseInstructionFloat(m)));
					instructions.add(Float.toString(rrx = parseInstructionFloat(m)));
					instructions.add(Float.toString(rry = parseInstructionFloat(m)));
					if (rrx == 0 || rry == 0) {
						p.append(new Rectangle2D.Float(rx, ry, rw, rh), false);
					} else {
						p.append(new RoundRectangle2D.Float(rx, ry, rw, rh, rrx, rry), false);
					}
					p.moveTo(lcx = lx, lcy = ly);
					break;
				case 'r':
					instructions.add("R");
					instructions.add(Float.toString(rx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(ry = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(rw = parseInstructionFloat(m)));
					instructions.add(Float.toString(rh = parseInstructionFloat(m)));
					instructions.add(Float.toString(rrx = parseInstructionFloat(m)));
					instructions.add(Float.toString(rry = parseInstructionFloat(m)));
					if (rrx == 0 || rry == 0) {
						p.append(new Rectangle2D.Float(rx, ry, rw, rh), false);
					} else {
						p.append(new RoundRectangle2D.Float(rx, ry, rw, rh, rrx, rry), false);
					}
					p.moveTo(lcx = lx, lcy = ly);
					break;
				case 'E':
					instructions.add("E");
					instructions.add(Float.toString(rx = parseInstructionFloat(m)));
					instructions.add(Float.toString(ry = parseInstructionFloat(m)));
					instructions.add(Float.toString(rw = parseInstructionFloat(m)));
					instructions.add(Float.toString(rh = parseInstructionFloat(m)));
					instructions.add(Float.toString(ras = parseInstructionFloat(m)));
					instructions.add(Float.toString(rae = parseInstructionFloat(m)));
					instructions.add(Integer.toString(rat = (Math.abs((int)Math.round(parseInstructionFloat(m))) % 5)));
					if (rae <= -360 || rae >= 360) {
						p.append(new Ellipse2D.Float(rx, ry, rw, rh), false);
						p.moveTo(lcx = lx, lcy = ly);
					} else if (rat < 3) {
						p.append(new Arc2D.Float(rx, ry, rw, rh, ras, rae, rat), false);
						p.moveTo(lcx = lx, lcy = ly);
					} else {
						p.append(new Arc2D.Float(rx, ry, rw, rh, ras, rae, Arc2D.OPEN), rat > 3);
						Point2D cp = p.getCurrentPoint();
						lcx = lx = (float)cp.getX();
						lcy = ly = (float)cp.getY();
					}
					break;
				case 'e':
					instructions.add("E");
					instructions.add(Float.toString(rx = lx + parseInstructionFloat(m)));
					instructions.add(Float.toString(ry = ly + parseInstructionFloat(m)));
					instructions.add(Float.toString(rw = parseInstructionFloat(m)));
					instructions.add(Float.toString(rh = parseInstructionFloat(m)));
					instructions.add(Float.toString(ras = parseInstructionFloat(m)));
					instructions.add(Float.toString(rae = parseInstructionFloat(m)));
					instructions.add(Integer.toString(rat = (Math.abs((int)Math.round(parseInstructionFloat(m))) % 5)));
					if (rae <= -360 || rae >= 360) {
						p.append(new Ellipse2D.Float(rx, ry, rw, rh), false);
						p.moveTo(lcx = lx, lcy = ly);
					} else if (rat < 3) {
						p.append(new Arc2D.Float(rx, ry, rw, rh, ras, rae, rat), false);
						p.moveTo(lcx = lx, lcy = ly);
					} else {
						p.append(new Arc2D.Float(rx, ry, rw, rh, ras, rae, Arc2D.OPEN), rat > 3);
						Point2D cp = p.getCurrentPoint();
						lcx = lx = (float)cp.getX();
						lcy = ly = (float)cp.getY();
					}
					break;
			}
		}
		StringBuffer sb = new StringBuffer();
		Iterator<String> ii = instructions.iterator();
		if (ii.hasNext()) {
			sb.append(ii.next());
		}
		while (ii.hasNext()) {
			sb.append(" ");
			sb.append(ii.next());
		}
		return sb.toString();
	}
	private static float parseInstructionFloat(Matcher m) {
		if (m.find()) {
			try {
				return Float.parseFloat(m.group());
			} catch (NumberFormatException nfe) {
				return 0.0f;
			}
		} else {
			return 0.0f;
		}
	}
	
	private static void arcTo(
		GeneralPath p, double rx, double ry, double a,
		boolean large, boolean sweep, double x, double y
	) {
		Point2D p0 = p.getCurrentPoint();
		double x0 = p0.getX();
		double y0 = p0.getY();
		if (x0 == x && y0 == y) return;
		if (rx == 0 || ry == 0) { p.lineTo(x, y); return; }
		double dx2 = (x0 - x) / 2;
		double dy2 = (y0 - y) / 2;
		a = Math.toRadians(a % 360);
		double ca = Math.cos(a);
		double sa = Math.sin(a);
		double x1 = sa * dy2 + ca * dx2;
		double y1 = ca * dy2 - sa * dx2;
		rx = Math.abs(rx);
		ry = Math.abs(ry);
		double Prx = rx * rx;
		double Pry = ry * ry;
		double Px1 = x1 * x1;
		double Py1 = y1 * y1;
		double rc = Px1/Prx + Py1/Pry;
		if (rc > 1) {
			rx = Math.sqrt(rc) * rx;
			ry = Math.sqrt(rc) * ry;
			Prx = rx * rx;
			Pry = ry * ry;
		}
		double s = (large == sweep) ? -1 : 1;
		double sq = ((Prx*Pry)-(Prx*Py1)-(Pry*Px1)) / ((Prx*Py1)+(Pry*Px1));
		if (sq < 0) sq = 0;
		double m = s * Math.sqrt(sq);
		double cx1 = m *  ((rx * y1) / ry);
		double cy1 = m * -((ry * x1) / rx);
		double sx2 = (x0 + x) / 2;
		double sy2 = (y0 + y) / 2;
		double cx = sx2 + ca * cx1 - sa * cy1;
		double cy = sy2 + sa * cx1 + ca * cy1;
		double ux = (x1 - cx1) / rx;
		double uy = (y1 - cy1) / ry;
		double vx = (-x1 -cx1) / rx;
		double vy = (-y1 -cy1) / ry;
		double sn = Math.sqrt(ux*ux + uy*uy);
		double sp = ux;
		double ss = (uy < 0) ? -1 : 1;
		double as = Math.toDegrees(ss * Math.acos(sp / sn));
		double en = Math.sqrt((ux*ux + uy*uy) * (vx*vx + vy*vy));
		double ep = ux * vx + uy * vy;
		double es = (ux * vy - uy * vx < 0) ? -1 : 1;
		double ae = Math.toDegrees(es * Math.acos(ep / en));
		if (!sweep && ae > 0) ae -= 360;
		if (sweep && ae < 0) ae += 360;
		ae %= 360;
		as %= 360;
		Arc2D.Double arc = new Arc2D.Double();
		arc.x = cx - rx;
		arc.y = cy - ry;
		arc.width = rx * 2;
		arc.height = ry * 2;
		arc.start = -as;
		arc.extent = -ae;
		double acx = arc.getCenterX();
		double acy = arc.getCenterY();
		AffineTransform t = AffineTransform.getRotateInstance(a, acx, acy);
		p.append(t.createTransformedShape(arc), true);
	}
	
	private static void coArcTo(GeneralPath p, double x2, double y2, double x3, double y3) {
		Point2D p1 = p.getCurrentPoint();
		double x1 = p1.getX();
		double y1 = p1.getY();
		boolean xe = (x1 == x2 && x2 == x3);
		boolean ye = (y1 == y2 && y2 == y3);
		if (xe && ye) return;
		if (xe || ye) { p.lineTo(x3, y3); return; }
		double d = arcHK(x1, y1, x2, y2, x3, y3);
		double h = arcH(x1, y1, x2, y2, x3, y3) / d;
		double k = arcK(x1, y1, x2, y2, x3, y3) / d;
		if (Double.isNaN(h) || Double.isInfinite(h)) { p.lineTo(x3, y3); return; }
		if (Double.isNaN(k) || Double.isInfinite(k)) { p.lineTo(x3, y3); return; }
		double r = Math.hypot(k - y1, x1 - h);
		double a1 = Math.toDegrees(Math.atan2(k - y1, x1 - h));
		double a2 = Math.toDegrees(Math.atan2(k - y2, x2 - h));
		double a3 = Math.toDegrees(Math.atan2(k - y3, x3 - h));
		Arc2D.Double arc = new Arc2D.Double();
		arc.x = h - r;
		arc.y = k - r;
		arc.width = r + r;
		arc.height = r + r;
		arc.start = a1;
		if ((a1 <= a2 && a2 <= a3) || (a3 <= a2 && a2 <= a1)) {
			arc.extent = a3 - a1;
		} else if (a3 <= a1) {
			arc.extent = a3 - a1 + 360;
		} else {
			arc.extent = a3 - a1 - 360;
		}
		p.append(arc, true);
	}
	private static double arcdet(double a, double b, double c, double d, double e, double f, double g, double h, double i) {
		return a*e*i + b*f*g + c*d*h - a*f*h - b*d*i - c*e*g;
	}
	private static double arcHK(double x1, double y1, double x2, double y2, double x3, double y3) {
		return arcdet(x1, y1, 1, x2, y2, 1, x3, y3, 1) * 2;
	}
	private static double arcH(double x1, double y1, double x2, double y2, double x3, double y3) {
		return arcdet(x1*x1 + y1*y1, y1, 1, x2*x2 + y2*y2, y2, 1, x3*x3 + y3*y3, y3, 1);
	}
	private static double arcK(double x1, double y1, double x2, double y2, double x3, double y3) {
		return arcdet(x1, x1*x1 + y1*y1, 1, x2, x2*x2 + y2*y2, 1, x3, x3*x3 + y3*y3, 1);
	}
}