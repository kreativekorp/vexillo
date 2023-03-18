package com.kreative.vexillo.style.fruit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.RenderedOp;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;
import com.kreative.vexillo.style.JAIUtils;
import com.kreative.vexillo.style.Stylizer;

public class FruitStylizer implements Stylizer {
	private static final ImageScaler scaler = ImageScaler.ITERATIVE_BICUBIC;
	private static final int[] dims = { 280, 180, 2, 12 };
	private static final int[] border = { 20, 20, 66, 60 };
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, -0.333508303f, 1, 3.893184497e-3f, 0, 0, -9.390406971e-6f, 0, 0, 0 };
	private static final float[] scale = { 0.5f, 0.5f, 0, 0 };
	
	private static final BufferedImage rectMin;
	private static final BufferedImage rectMax;
	static {
		BufferedImage rectMinTemp = null;
		BufferedImage rectMaxTemp = null;
		try { rectMinTemp = ImageIO.read(FruitStylizer.class.getResource("fruitstylizer-min.png")); }
		catch (Exception e) { e.printStackTrace(); }
		try { rectMaxTemp = ImageIO.read(FruitStylizer.class.getResource("fruitstylizer-max.png")); }
		catch (Exception e) { e.printStackTrace(); }
		rectMin = rectMinTemp;
		rectMax = rectMaxTemp;
	}
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int w, int h, ImageScaler unused2, int unused3, int unused4) {
		BufferedImage i = r.renderToImage(dims[0], dims[1], scaler, dims[2], 0);
		RenderedOp bi = JAIUtils.border(i, border[0], border[1], border[2], border[3], BorderExtender.BORDER_COPY);
		RenderedOp wi = JAIUtils.warp(bi, xCoeffs, yCoeffs);
		RenderedOp si = JAIUtils.scale(wi, scale[0], scale[1], scale[2], scale[3]);
		if (r.isRectangular() && rectMin != null && rectMax != null) {
			i = JAIUtils.maxMin(si.getAsBufferedImage(), rectMax, rectMin);
		} else try {
			Shape ts = r.getBoundaryShape(0, border[2], dims[0], dims[1]);
			int tw = dims[0], th = dims[1] + border[2] + border[3];
			BufferedImage m = createRange(ts, tw, th);
			RenderedOp bm = JAIUtils.border(m, border[0], border[1], 0, 0, BorderExtender.BORDER_ZERO);
			RenderedOp wm = JAIUtils.warp(bm, xCoeffs, yCoeffs);
			RenderedOp sm = JAIUtils.scale(wm, scale[0], scale[1], scale[2], scale[3]);
			BufferedImage b = createMin(ts, tw, th);
			RenderedOp bb = JAIUtils.border(b, border[0], border[1], 0, 0, BorderExtender.BORDER_ZERO);
			RenderedOp wb = JAIUtils.warp(bb, xCoeffs, yCoeffs);
			RenderedOp sb = JAIUtils.scale(wb, scale[0], scale[1], scale[2], scale[3]);
			i = JAIUtils.multiplyAdd(si.getAsBufferedImage(), sm.getAsBufferedImage(), sb.getAsBufferedImage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ImageScaler.ITERATIVE_BICUBIC.scale(i, w, h);
	}
	
	private static BufferedImage createRange(Shape outer, int w, int h) {
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[w]; for (int i = 0; i < w; i++) rgb[i] = 0xCCCCCC;
		tmpl.setRGB(0, 0, w, h, rgb, 0, 0);
		
		Graphics2D g = tmpl.createGraphics();
		JAIUtils.prep(g);
		g.clip(outer);
		g.setColor(new Color(0xCCCCCC));
		g.fillRect(0, 0, w, h);
		
		// inner round rectangle gradient
		Shape inner = contract(outer, 70);
		for (int v = 205, d = 216; v <= 217; d -= 16, v++) {
			g.setColor(new Color(v, v, v));
			g.setStroke(new BasicStroke(d, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(inner);
		}
		g.fill(inner);
		
		// outer rectangle gradient
		for (int v = 203, d = 24; d > 0; d -= 3, v -= 7) {
			g.setColor(new Color(v, v, v));
			g.setStroke(new BasicStroke(d));
			g.draw(outer);
		}
		
		// top and bottom gradient
		new EdgeEffect(10, 1) {
			private final Line2D.Float line = new Line2D.Float();
			public void apply(Graphics2D g, Shape s, float x1, float y1, float x2, float y2) {
				if (isTopEdge(s, x1, y1, x2, y2)) {
					line.setLine(x1, y1, x2, y2);
					g.setColor(new Color(0, 0, 0, 10));
					for (int d = 28; d > 0; d -= 2) {
						g.setStroke(new BasicStroke(d));
						g.draw(line);
					}
				}
				if (isBottomEdge(s, x1, y1, x2, y2)) {
					line.setLine(x1, y1, x2, y2);
					g.setColor(new Color(0, 0, 0, 6));
					for (int d = 28; d > 0; d -= 2) {
						g.setStroke(new BasicStroke(d));
						g.draw(line);
					}
				}
			}
		}.apply(g, outer);
		
		g.dispose();
		return tmpl;
	}
	
	private static BufferedImage createMin(Shape outer, int w, int h) {
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tmpl.createGraphics();
		JAIUtils.prep(g);
		
		// linear gradient
		g.setPaint(new LinearGradientPaint(
			new Point(0, h/2),
			new Point(w, h/2),
			new float[]{ 0, 0.46f, 0.635f, 0.87f, 0.9f, 0.965f },
			new Color[]{
				new Color(0, 0, 0),
				new Color(38, 38, 38),
				new Color(38, 38, 38),
				new Color(0, 0, 0),
				new Color(0, 0, 0),
				new Color(22, 22, 22),
			}
		));
		g.fillRect(0, 0, w, h);
		g.setPaint(new Color(21, 21, 21));
		g.fillRect(w-4, 0, 2, h);
		
		// highlights and other weird stuff
		new EdgeEffect(10, 1) {
			public void apply(Graphics2D g, Shape s, float x1, float y1, float x2, float y2) {
				if (isTopEdge(s, x1, y1, x2, y2)) {
					g.setColor(new Color(0, 0, 0, 6));
					g.fill(align(top1VHilite, x1, y1, x2, y2, x1 > x2));
					
					g.setColor(new Color(255, 255, 255, 11));
					for (int i = 2; i <= 10; i++) {
						g.fill(align(top70VHilite(i / 10.0), x1, y1, x2, y2, x1 > x2));
					}
				}
				if (isBottomEdge(s, x1, y1, x2, y2)) {
					g.setColor(new Color(0, 0, 0, 6));
					g.fill(align(bot1VHilite, x1, y1, x2, y2, x1 > x2));
					
					g.setColor(new Color(0, 0, 0, 17));
					for (int i = 2; i <= 10; i++) {
						g.fill(align(bot18VHilite(i / 10.0), x1, y1, x2, y2, x1 > x2));
					}
					
					g.setColor(new Color(0, 0, 0, 9));
					for (int i = 2; i <= 10; i++) {
						g.fill(align(botPointHilite(i / 10.0), x1, y1, x2, y2, x1 > x2));
					}
				}
			}
		}.apply(g, outer);
		
		g.dispose();
		return tmpl;
	}
	
	private static Shape contract(Shape s, int t) {
		if (t < 1) return s;
		Shape ss = new BasicStroke(t * 2).createStrokedShape(s);
		Area cs = new Area(s); cs.subtract(new Area(ss));
		return cs;
	}
	
	private static Shape align(Shape s, float x1, float y1, float x2, float y2, boolean swap) {
		if (swap) return align(s, x2, y2, x1, y1, false);
		double scale = Math.hypot(y2 - y1, x2 - x1);
		double angle = Math.atan2(y2 - y1, x2 - x1);
		AffineTransform tx = new AffineTransform();
		tx.translate(x1, y1);
		tx.scale(scale, scale);
		tx.rotate(angle);
		return tx.createTransformedShape(s);
	}
	
	private static Shape top70VHilite(double grad) {
		double y = 0.05 * grad;
		double x = 0.05 * (1 - grad);
		GeneralPath p = new GeneralPath();
		p.moveTo(x, 0);
		p.curveTo(x, 0, x, y, 0.057, y);
		p.lineTo(0.943, y);
		p.curveTo(1-x, y, 1-x, 0, 1-x, 0);
		p.closePath();
		return p;
	}
	
	private static Shape bot18VHilite(double grad) {
		double y = -0.05 * grad;
		double x1 = 0.453 - 0.413 * grad;
		double x2 = 0.453 + 0.413 * grad;
		GeneralPath p = new GeneralPath();
		p.moveTo(x1, 0);
		p.curveTo(x1, y / 2, x1, y, 0.453, y);
		p.curveTo(x2, y, x2, y / 2, x2, 0);
		p.closePath();
		return p;
	}
	
	private static Shape botPointHilite(double grad) {
		double d = 0.1 * grad, y = -d / 2, x = 0.97 + y;
		return new Ellipse2D.Double(x, y, d, d);
	}
	
	private static final GeneralPath top1VHilite;
	private static final GeneralPath bot1VHilite;
	static {
		top1VHilite = new GeneralPath();
		top1VHilite.moveTo(0.165, 0);
		top1VHilite.curveTo(0.165, 0, 0.234, 0.132, 0.5, 0.132);
		top1VHilite.curveTo(0.766, 0.132, 0.835, 0, 0.835, 0);
		top1VHilite.closePath();
		bot1VHilite = new GeneralPath();
		bot1VHilite.moveTo(0.114, 0);
		bot1VHilite.curveTo(0.114, 0, 0.2, -0.132, 0.466, -0.132);
		bot1VHilite.curveTo(0.732, -0.132, 0.818, 0, 0.818, 0);
		bot1VHilite.closePath();
	}
}