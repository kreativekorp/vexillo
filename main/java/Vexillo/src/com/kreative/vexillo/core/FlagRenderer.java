package com.kreative.vexillo.core;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class FlagRenderer {
	private static final java.awt.Color topGlaze = new java.awt.Color(255, 255, 255, 51);
	private static final java.awt.Color topMiddleGlaze = new java.awt.Color(255, 255, 255, 0);
	private static final java.awt.Color bottomMiddleGlaze = new java.awt.Color(0, 0, 0, 0);
	private static final java.awt.Color bottomGlaze = new java.awt.Color(0, 0, 0, 51);
	private static final java.awt.Color innerGlaze = new java.awt.Color(255, 255, 255, 102);
	private static final java.awt.Color outerGlaze = new java.awt.Color(0, 0, 0, 102);
	
	private final File parent;
	private final Flag flag;
	private final Map<String, Shape> shapeCache;
	private final Map<String, BufferedImage> imageCache;
	
	public FlagRenderer(File parent, Flag flag) {
		this.parent = parent;
		this.flag = flag;
		this.shapeCache = new HashMap<String, Shape>();
		this.imageCache = new HashMap<String, BufferedImage>();
	}
	
	public void renderToFile(File out, String format, int w, int h, int supersample, int glaze) throws IOException {
		BufferedImage img = renderToImage(w, h, supersample, glaze);
		ImageIO.write(img, format, out);
	}
	
	public BufferedImage renderToImage(int w, int h, int supersample, int glaze) {
		BufferedImage img;
		if (supersample > 1) {
			img = new BufferedImage(w * supersample, h * supersample, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			render(g, 0, 0, w * supersample, h * supersample);
			g.dispose();
			img = ImageUtils.scale(img, w, h);
		} else {
			img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			render(g, 0, 0, w, h);
			g.dispose();
		}
		if (glaze > 0) {
			Graphics2D g = img.createGraphics();
			glaze(g, 0, 0, w, h, glaze);
			g.dispose();
		}
		return img;
	}
	
	public void render(Graphics2D g, int x, int y, int w, int h) {
		prep(g);
		Map<String, Dimension> d = flag.createNamespace(h, w);
		for (Instruction i : flag.instructions()) execute(i, d, g, x, y, w, h);
	}
	
	private void prep(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	private void execute(Instruction i, Map<String, Dimension> d, Graphics2D g, int x, int y, int w, int h) {
		if (i instanceof Instruction.GroupInstruction) {
			Instruction.GroupInstruction gi = (Instruction.GroupInstruction)i;
			if (gi.clippingRegion.isEmpty()) {
				for (Instruction j : gi.instructions) execute(j, d, g, x, y, w, h);
			} else {
				Shape clip = createClippingRegion(gi.clippingRegion, d, 0, 0);
				if (clip == null || clip.getBounds().isEmpty()) {
					for (Instruction j : gi.instructions) execute(j, d, g, x, y, w, h);
				} else {
					BufferedImage ti1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D tg1 = ti1.createGraphics();
					prep(tg1);
					for (Instruction j : gi.instructions) execute(j, d, tg1, 0, 0, w, h);
					tg1.dispose();
					
					BufferedImage ti2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					Graphics2D tg2 = ti2.createGraphics();
					prep(tg2);
					tg2.setColor(java.awt.Color.WHITE);
					tg2.fill(clip);
					tg2.setComposite(AlphaComposite.SrcIn);
					tg2.drawImage(ti1, null, 0, 0);
					tg2.dispose();
					
					g.drawImage(ti2, null, x, y);
				}
			}
		} else if (i instanceof Instruction.ForInstruction) {
			Instruction.ForInstruction fi = (Instruction.ForInstruction)i;
			double start = fi.start.value(d);
			double end = fi.end.value(d);
			double step = fi.step.value(d);
			while (start <= end) {
				d.put(fi.var, new Dimension.Constant(start));
				for (Instruction j : fi.instructions) execute(j, d, g, x, y, w, h);
				start += step;
			}
		} else if (i instanceof Instruction.FieldInstruction) {
			Instruction.FieldInstruction fi = (Instruction.FieldInstruction)i;
			int x1 = (int)Math.round(fi.x1.value(d));
			int y1 = (int)Math.round(fi.y1.value(d));
			int x2 = (int)Math.round(fi.x2.value(d));
			int y2 = (int)Math.round(fi.y2.value(d));
			Color color = flag.colors().get(fi.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				g.fillRect(x + x1, y + y1, x2 - x1, y2 - y1);
			}
		} else if (i instanceof Instruction.HBandInstruction) {
			Instruction.HBandInstruction bi = (Instruction.HBandInstruction)i;
			int x1 = (int)Math.round(bi.x1.value(d));
			int x2 = (int)Math.round(bi.x2.value(d));
			double y1 = bi.y1.value(d);
			double height = bi.y2.value(d) - y1;
			int currentWeight = 0;
			int totalWeight = bi.getBandWeightTotal();
			for (int band = 0; band < bi.bands; band++) {
				int bandTop = (int)Math.round(y1 + height * currentWeight / totalWeight);
				currentWeight += bi.getBandWeight(band);
				int bandBottom = (int)Math.round(y1 + height * currentWeight / totalWeight);
				Color color = flag.colors().get(bi.getBandColor(band));
				if (color instanceof Color.HasRGB) {
					g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
					g.fillRect(x + x1, y + bandTop, x2 - x1, bandBottom - bandTop);
				}
			}
		} else if (i instanceof Instruction.VBandInstruction) {
			Instruction.VBandInstruction bi = (Instruction.VBandInstruction)i;
			int y1 = (int)Math.round(bi.y1.value(d));
			int y2 = (int)Math.round(bi.y2.value(d));
			double x1 = bi.x1.value(d);
			double width = bi.x2.value(d) - x1;
			int currentWeight = 0;
			int totalWeight = bi.getBandWeightTotal();
			for (int band = 0; band < bi.bands; band++) {
				int bandLeft = (int)Math.round(x1 + width * currentWeight / totalWeight);
				currentWeight += bi.getBandWeight(band);
				int bandRight = (int)Math.round(x1 + width * currentWeight / totalWeight);
				Color color = flag.colors().get(bi.getBandColor(band));
				if (color instanceof Color.HasRGB) {
					g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
					g.fillRect(x + bandLeft, y + y1, bandRight - bandLeft, y2 - y1);
				}
			}
		} else if (i instanceof Instruction.CrossInstruction) {
			Instruction.CrossInstruction ci = (Instruction.CrossInstruction)i;
			int x1 = (int)Math.round(ci.x1.value(d));
			int y1 = (int)Math.round(ci.y1.value(d));
			int x2 = (int)Math.round(ci.x2.value(d));
			int y2 = (int)Math.round(ci.y2.value(d));
			int x3 = (int)Math.round(ci.x3.value(d));
			int y3 = (int)Math.round(ci.y3.value(d));
			int x4 = (int)Math.round(ci.x4.value(d));
			int y4 = (int)Math.round(ci.y4.value(d));
			Color color = flag.colors().get(ci.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				g.fillRect(x + x1, y + y2, x4 - x1, y3 - y2);
				g.fillRect(x + x2, y + y1, x3 - x2, y4 - y1);
			}
		} else if (i instanceof Instruction.SaltireInstruction) {
			Instruction.SaltireInstruction si = (Instruction.SaltireInstruction)i;
			int x1 = (int)Math.round(si.x1.value(d));
			int y1 = (int)Math.round(si.y1.value(d));
			int x2 = (int)Math.round(si.x2.value(d));
			int y2 = (int)Math.round(si.y2.value(d));
			int thickness = (int)Math.round(si.thickness.value(d));
			if (thickness < 0) thickness = 0;
			Color color = flag.colors().get(si.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				Shape clip = g.getClip();
				Stroke stroke = g.getStroke();
				g.clipRect(x + x1, y + y1, x2 - x1, y2 - y1);
				g.setStroke(new BasicStroke(thickness));
				g.drawLine(x + x1, y + y1, x + x2, y + y2);
				g.drawLine(x + x1, y + y2, x + x2, y + y1);
				g.setClip(clip);
				g.setStroke(stroke);
			}
		} else if (i instanceof Instruction.DBandInstruction) {
			Instruction.DBandInstruction bi = (Instruction.DBandInstruction)i;
			int x1 = (int)Math.round(bi.x1.value(d));
			int y1 = (int)Math.round(bi.y1.value(d));
			int x2 = (int)Math.round(bi.x2.value(d));
			int y2 = (int)Math.round(bi.y2.value(d));
			int thickness = (int)Math.round(bi.thickness.value(d));
			if (thickness < 0) thickness = 0;
			Color color = flag.colors().get(bi.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				Stroke stroke = g.getStroke();
				g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10));
				g.drawLine(x + x1, y + y1, x + x2, y + y2);
				g.setStroke(stroke);
			}
		} else if (i instanceof Instruction.DiscInstruction) {
			Instruction.DiscInstruction di = (Instruction.DiscInstruction)i;
			int centerX = (int)Math.round(di.centerX.value(d));
			int centerY = (int)Math.round(di.centerY.value(d));
			double width = di.width.value(d);
			double height = di.height.value(d);
			int x1 = (int)Math.round(centerX - width / 2);
			int y1 = (int)Math.round(centerY - height / 2);
			int x2 = (int)Math.round(centerX + width / 2);
			int y2 = (int)Math.round(centerY + height / 2);
			int startAngle = (int)Math.round(di.startAngle.value(d));
			int endAngle = (int)Math.round(di.endAngle.value(d));
			Color color = flag.colors().get(di.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				g.fillArc(x1, y1, x2 - x1, y2 - y1, startAngle, endAngle - startAngle);
			}
		} else if (i instanceof Instruction.PolyInstruction) {
			Instruction.PolyInstruction pi = (Instruction.PolyInstruction)i;
			int[] xCoords = new int[pi.points];
			int[] yCoords = new int[pi.points];
			for (int idx = 0; idx < pi.points; idx++) {
				xCoords[idx] = (int)Math.round(pi.x[idx].value(d));
				yCoords[idx] = (int)Math.round(pi.y[idx].value(d));
			}
			Color color = flag.colors().get(pi.color);
			if (color instanceof Color.HasRGB) {
				g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
				g.fillPolygon(xCoords, yCoords, pi.points);
			}
		} else if (i instanceof Instruction.SymbolInstruction) {
			Instruction.SymbolInstruction si = (Instruction.SymbolInstruction)i;
			Shape sh = null;
			if (shapeCache.containsKey(si.symbolName)) {
				sh = shapeCache.get(si.symbolName);
			} else {
				Symbol s = flag.symbols().get(si.symbolName);
				if (s != null) {
					sh = s.toPath();
					shapeCache.put(si.symbolName, sh);
				}
			}
			if (sh != null) {
				double shr = si.rotate.value(d);
				if (shr != 0) sh = AffineTransform.getRotateInstance(Math.toRadians(shr)).createTransformedShape(sh);
				double shsx = si.sx.value(d);
				double shsy = si.sy.value(d);
				sh = AffineTransform.getScaleInstance(shsx, shsy).createTransformedShape(sh);
				double shx = si.x.value(d);
				double shy = si.y.value(d);
				sh = AffineTransform.getTranslateInstance(x + shx, y + shy).createTransformedShape(sh);
				Color color = flag.colors().get(si.color);
				if (color instanceof Color.HasRGB) {
					g.setColor(new java.awt.Color(((Color.HasRGB)color).getRGB()));
					g.fill(sh);
				}
			}
		} else if (i instanceof Instruction.ImageInstruction) {
			Instruction.ImageInstruction ii = (Instruction.ImageInstruction)i;
			BufferedImage image = null;
			if (imageCache.containsKey(ii.imageName)) {
				image = imageCache.get(ii.imageName);
			} else if (flag.images().containsKey(ii.imageName)) {
				for (ImageSource src : flag.images().get(ii.imageName)) {
					try {
						image = ImageIO.read(src.getInputStream(parent));
						if (image != null) {
							imageCache.put(ii.imageName, image);
							break;
						}
					} catch (IOException e) {
						image = null;
					}
				}
			}
			if (image != null) {
				double x1 = ii.x1.value(d);
				double y1 = ii.y1.value(d);
				double x2 = ii.x2.value(d);
				double y2 = ii.y2.value(d);
				double width = x2 - x1;
				double height = y2 - y1;
				double imageHeightWithinWidth = width * (double)image.getHeight() / (double)image.getWidth();
				double imageWidthWithinHeight = height * (double)image.getWidth() / (double)image.getHeight();
				if (imageHeightWithinWidth <= height) {
					int l = (int)Math.round(x1);
					int r = (int)Math.round(x2);
					int t = (int)Math.round(y1 + (height - imageHeightWithinWidth)/2.0);
					int b = (int)Math.round(y1 + (height + imageHeightWithinWidth)/2.0);
					if ((r - l) > 0 && (b - t) > 0) {
						image = ImageUtils.scale(image, r - l, b - t);
						g.drawImage(image, null, l, t);
					}
				} else if (imageWidthWithinHeight <= width) {
					int t = (int)Math.round(y1);
					int b = (int)Math.round(y2);
					int l = (int)Math.round(x1 + (width - imageWidthWithinHeight)/2.0);
					int r = (int)Math.round(x1 + (width + imageWidthWithinHeight)/2.0);
					if ((r - l) > 0 && (b - t) > 0) {
						image = ImageUtils.scale(image, r - l, b - t);
						g.drawImage(image, null, l, t);
					}
				}
			}
		} else if (i instanceof Instruction.HGradInstruction) {
			Instruction.HGradInstruction gi = (Instruction.HGradInstruction)i;
			int x1 = (int)Math.round(gi.x1.value(d));
			int y1 = (int)Math.round(gi.y1.value(d));
			int x2 = (int)Math.round(gi.x2.value(d));
			int y2 = (int)Math.round(gi.y2.value(d));
			Color color1 = flag.colors().get(gi.color1);
			Color color2 = flag.colors().get(gi.color2);
			if (color1 instanceof Color.HasRGB && color2 instanceof Color.HasRGB) {
				java.awt.Color c1 = new java.awt.Color(((Color.HasRGB)color1).getRGB());
				java.awt.Color c2 = new java.awt.Color(((Color.HasRGB)color2).getRGB());
				g.setPaint(new GradientPaint(x + x1, y + y1, c1, x + x1, y + y2, c2));
				g.fillRect(x + x1, y + y1, x2 - x1, y2 - y1);
			}
		} else if (i instanceof Instruction.VGradInstruction) {
			Instruction.VGradInstruction gi = (Instruction.VGradInstruction)i;
			int x1 = (int)Math.round(gi.x1.value(d));
			int y1 = (int)Math.round(gi.y1.value(d));
			int x2 = (int)Math.round(gi.x2.value(d));
			int y2 = (int)Math.round(gi.y2.value(d));
			Color color1 = flag.colors().get(gi.color1);
			Color color2 = flag.colors().get(gi.color2);
			if (color1 instanceof Color.HasRGB && color2 instanceof Color.HasRGB) {
				java.awt.Color c1 = new java.awt.Color(((Color.HasRGB)color1).getRGB());
				java.awt.Color c2 = new java.awt.Color(((Color.HasRGB)color2).getRGB());
				g.setPaint(new GradientPaint(x + x1, y + y1, c1, x + x2, y + y1, c2));
				g.fillRect(x + x1, y + y1, x2 - x1, y2 - y1);
			}
		}
	}
	
	private Shape createClippingRegion(List<Instruction> ii, Map<String, Dimension> d, int x, int y) {
		if (ii.size() == 1) {
			return createClippingRegion(ii.get(0), d, x, y);
		} else {
			Area a = new Area();
			for (Instruction i : ii) {
				Shape s = createClippingRegion(i, d, x, y);
				a.add((s instanceof Area) ? (Area)s : new Area(s));
			}
			return a;
		}
	}
	
	private Shape createClippingRegion(Instruction i, Map<String, Dimension> d, int x, int y) {
		if (i instanceof Instruction.GroupInstruction) {
			Instruction.GroupInstruction gi = (Instruction.GroupInstruction)i;
			Shape s = createClippingRegion(gi.instructions, d, x, y);
			if (gi.clippingRegion.isEmpty()) return s;
			Shape c = createClippingRegion(gi.clippingRegion, d, x, y);
			Area a = (s instanceof Area) ? (Area)s : new Area(s);
			a.intersect((c instanceof Area) ? (Area)c : new Area(c));
			return a;
		} else if (i instanceof Instruction.ForInstruction) {
			Instruction.ForInstruction fi = (Instruction.ForInstruction)i;
			Area a = new Area();
			double start = fi.start.value(d);
			double end = fi.end.value(d);
			double step = fi.step.value(d);
			while (start <= end) {
				d.put(fi.var, new Dimension.Constant(start));
				Shape s = createClippingRegion(fi.instructions, d, x, y);
				a.add((s instanceof Area) ? (Area)s : new Area(s));
				start += step;
			}
			return a;
		} else if (i instanceof Instruction.FieldInstruction) {
			Instruction.FieldInstruction fi = (Instruction.FieldInstruction)i;
			int x1 = (int)Math.round(fi.x1.value(d));
			int y1 = (int)Math.round(fi.y1.value(d));
			int x2 = (int)Math.round(fi.x2.value(d));
			int y2 = (int)Math.round(fi.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else if (i instanceof Instruction.HBandInstruction) {
			Instruction.HBandInstruction bi = (Instruction.HBandInstruction)i;
			int x1 = (int)Math.round(bi.x1.value(d));
			int y1 = (int)Math.round(bi.y1.value(d));
			int x2 = (int)Math.round(bi.x2.value(d));
			int y2 = (int)Math.round(bi.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else if (i instanceof Instruction.VBandInstruction) {
			Instruction.VBandInstruction bi = (Instruction.VBandInstruction)i;
			int x1 = (int)Math.round(bi.x1.value(d));
			int y1 = (int)Math.round(bi.y1.value(d));
			int x2 = (int)Math.round(bi.x2.value(d));
			int y2 = (int)Math.round(bi.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else if (i instanceof Instruction.CrossInstruction) {
			Instruction.CrossInstruction ci = (Instruction.CrossInstruction)i;
			int x1 = (int)Math.round(ci.x1.value(d));
			int y1 = (int)Math.round(ci.y1.value(d));
			int x2 = (int)Math.round(ci.x2.value(d));
			int y2 = (int)Math.round(ci.y2.value(d));
			int x3 = (int)Math.round(ci.x3.value(d));
			int y3 = (int)Math.round(ci.y3.value(d));
			int x4 = (int)Math.round(ci.x4.value(d));
			int y4 = (int)Math.round(ci.y4.value(d));
			Area a = new Area(new Rectangle(x + x1, y + y2, x4 - x1, y3 - y2));
			a.add(new Area(new Rectangle(x + x2, y + y1, x3 - x2, y4 - y1)));
			return a;
		} else if (i instanceof Instruction.SaltireInstruction) {
			Instruction.SaltireInstruction si = (Instruction.SaltireInstruction)i;
			int x1 = (int)Math.round(si.x1.value(d));
			int y1 = (int)Math.round(si.y1.value(d));
			int x2 = (int)Math.round(si.x2.value(d));
			int y2 = (int)Math.round(si.y2.value(d));
			int thickness = (int)Math.round(si.thickness.value(d));
			if (thickness < 0) thickness = 0;
			Stroke stroke = new BasicStroke(thickness);
			Area a = new Area(stroke.createStrokedShape(new Line2D.Double(x + x1, y + y1, x + x2, y + y2)));
			a.add(new Area(stroke.createStrokedShape(new Line2D.Double(x + x1, y + y2, x + x2, y + y1))));
			a.intersect(new Area(new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1)));
			return a;
		} else if (i instanceof Instruction.DBandInstruction) {
			Instruction.DBandInstruction bi = (Instruction.DBandInstruction)i;
			int x1 = (int)Math.round(bi.x1.value(d));
			int y1 = (int)Math.round(bi.y1.value(d));
			int x2 = (int)Math.round(bi.x2.value(d));
			int y2 = (int)Math.round(bi.y2.value(d));
			int thickness = (int)Math.round(bi.thickness.value(d));
			if (thickness < 0) thickness = 0;
			Stroke stroke = new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10);
			return stroke.createStrokedShape(new Line2D.Double(x + x1, y + y1, x + x2, y + y2));
		} else if (i instanceof Instruction.DiscInstruction) {
			Instruction.DiscInstruction di = (Instruction.DiscInstruction)i;
			int centerX = (int)Math.round(di.centerX.value(d));
			int centerY = (int)Math.round(di.centerY.value(d));
			double width = di.width.value(d);
			double height = di.height.value(d);
			int x1 = (int)Math.round(centerX - width / 2);
			int y1 = (int)Math.round(centerY - height / 2);
			int x2 = (int)Math.round(centerX + width / 2);
			int y2 = (int)Math.round(centerY + height / 2);
			int startAngle = (int)Math.round(di.startAngle.value(d));
			int endAngle = (int)Math.round(di.endAngle.value(d));
			return new Arc2D.Double(x1, y1, x2 - x1, y2 - y1, startAngle, endAngle - startAngle, Arc2D.PIE);
		} else if (i instanceof Instruction.PolyInstruction) {
			Instruction.PolyInstruction pi = (Instruction.PolyInstruction)i;
			int[] xCoords = new int[pi.points];
			int[] yCoords = new int[pi.points];
			for (int idx = 0; idx < pi.points; idx++) {
				xCoords[idx] = (int)Math.round(pi.x[idx].value(d));
				yCoords[idx] = (int)Math.round(pi.y[idx].value(d));
			}
			return new Polygon(xCoords, yCoords, pi.points);
		} else if (i instanceof Instruction.SymbolInstruction) {
			Instruction.SymbolInstruction si = (Instruction.SymbolInstruction)i;
			Shape sh = null;
			if (shapeCache.containsKey(si.symbolName)) {
				sh = shapeCache.get(si.symbolName);
			} else {
				Symbol s = flag.symbols().get(si.symbolName);
				if (s != null) {
					sh = s.toPath();
					shapeCache.put(si.symbolName, sh);
				}
			}
			if (sh != null) {
				double shr = si.rotate.value(d);
				if (shr != 0) sh = AffineTransform.getRotateInstance(Math.toRadians(shr)).createTransformedShape(sh);
				double shsx = si.sx.value(d);
				double shsy = si.sy.value(d);
				sh = AffineTransform.getScaleInstance(shsx, shsy).createTransformedShape(sh);
				double shx = si.x.value(d);
				double shy = si.y.value(d);
				sh = AffineTransform.getTranslateInstance(x + shx, y + shy).createTransformedShape(sh);
			}
			return sh;
		} else if (i instanceof Instruction.ImageInstruction) {
			Instruction.ImageInstruction ii = (Instruction.ImageInstruction)i;
			int x1 = (int)Math.round(ii.x1.value(d));
			int y1 = (int)Math.round(ii.y1.value(d));
			int x2 = (int)Math.round(ii.x2.value(d));
			int y2 = (int)Math.round(ii.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else if (i instanceof Instruction.HGradInstruction) {
			Instruction.HGradInstruction gi = (Instruction.HGradInstruction)i;
			int x1 = (int)Math.round(gi.x1.value(d));
			int y1 = (int)Math.round(gi.y1.value(d));
			int x2 = (int)Math.round(gi.x2.value(d));
			int y2 = (int)Math.round(gi.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else if (i instanceof Instruction.VGradInstruction) {
			Instruction.VGradInstruction gi = (Instruction.VGradInstruction)i;
			int x1 = (int)Math.round(gi.x1.value(d));
			int y1 = (int)Math.round(gi.y1.value(d));
			int x2 = (int)Math.round(gi.x2.value(d));
			int y2 = (int)Math.round(gi.y2.value(d));
			return new Rectangle(x + x1, y + y1, x2 - x1, y2 - y1);
		} else {
			return null;
		}
	}
	
	public void glaze(Graphics2D g, int x, int y, int w, int h, int t) {
		prep(g);
		Map<String, Dimension> d = flag.createNamespace(h, w);
		int gx = d.containsKey(".glazeleft") ? (x + (int)Math.round(d.get(".glazeleft").value(d))) : x;
		int gy = d.containsKey(".glazetop") ? (y + (int)Math.round(d.get(".glazetop").value(d))) : y;
		int gw = d.containsKey(".glazeright") ? (x + (int)Math.round(d.get(".glazeright").value(d)) - gx) : w;
		int gh = d.containsKey(".glazebottom") ? (y + (int)Math.round(d.get(".glazebottom").value(d)) - gy) : h;
		if (gw > 0 && gh > 0) {
			if (flag.symbols().containsKey(".glazearea")) {
				Shape sh = flag.symbols().get(".glazearea").toPath();
				sh = AffineTransform.getScaleInstance(gw, gh).createTransformedShape(sh);
				sh = AffineTransform.getTranslateInstance(gx, gy).createTransformedShape(sh);
				g.setPaint(new GradientPaint(gx, gy, topGlaze, gx+gw/2, gy+gh/2, topMiddleGlaze));
				g.fill(sh);
				g.setPaint(new GradientPaint(gx+gw/2, gy+gh/2, bottomMiddleGlaze, gx+gw, gy+gh, bottomGlaze));
				g.fill(sh);
				Shape sh2 = new BasicStroke(t * 2).createStrokedShape(sh);
				Shape sh3 = new BasicStroke(t * 4).createStrokedShape(sh);
				Area outer = new Area(sh); outer.intersect(new Area(sh2));
				Area inner = new Area(sh); inner.intersect(new Area(sh3)); inner.subtract(outer);
				g.setPaint(innerGlaze); g.fill(inner);
				g.setPaint(outerGlaze); g.fill(outer);
			} else {
				g.setPaint(new GradientPaint(gx, gy, topGlaze, gx+gw/2, gy+gh/2, topMiddleGlaze));
				g.fillRect(gx, gy, gw, gh);
				g.setPaint(new GradientPaint(gx+gw/2, gy+gh/2, bottomMiddleGlaze, gx+gw, gy+gh, bottomGlaze));
				g.fillRect(gx, gy, gw, gh);
				Rectangle r1 = new Rectangle(gx, gy, gw, gh);
				Rectangle r2 = new Rectangle(gx+t, gy+t, gw-t*2, gh-t*2);
				Rectangle r3 = new Rectangle(gx+t*2, gy+t*2, gw-t*4, gh-t*4);
				Area outer = new Area(r1); outer.subtract(new Area(r2));
				Area inner = new Area(r2); inner.subtract(new Area(r3));
				g.setPaint(innerGlaze); g.fill(inner);
				g.setPaint(outerGlaze); g.fill(outer);
			}
		}
	}
}