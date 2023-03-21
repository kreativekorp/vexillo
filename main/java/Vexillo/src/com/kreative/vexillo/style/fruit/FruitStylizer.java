package com.kreative.vexillo.style.fruit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
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
		g.clip(outer);
		g.setColor(new Color(0xCCCCCC));
		g.fillRect(0, 0, w, h);
		
		// inner round rectangle gradient
		Shape inner = contract(outer, 70);
		for (int i = 0; i < rrectGrad.length;) {
			int v = rrectGrad[i++];
			int d = rrectGrad[i++] * 2;
			g.setColor(new Color(v, v, v));
			g.setStroke(new BasicStroke(d, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(inner);
		}
		g.fill(inner);
		
		// outer rectangle gradient
		for (int i = 0; i < outGrad.length;) {
			int v = outGrad[i++];
			int d = outGrad[i++] * 2;
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
					g.setColor(new Color(0, 0, 0, 9));
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
		// linear gradient
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[w];
		for (int x = 0, i = 0; i < linGrad.length;) {
			int count = linGrad[i++] * 2;
			int value = (linGrad[i++] * 0x010101) | (0xFF << 24);
			while (count-- > 0) rgb[x++] = value;
		}
		tmpl.setRGB(0, 0, w, h, rgb, 0, 0);
		
		// highlights and other weird stuff
		Graphics2D g = tmpl.createGraphics();
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
	
	private static final int[] linGrad = {
		2, 0, 3, 1, 2, 2, 2, 3, 2, 4, 1, 5, 2, 6, 2, 7, 2, 8, 1, 9,
		1, 10, 2, 11, 2, 12, 1, 13, 1, 14, 2, 15, 1, 16, 1, 17, 2, 18,
		1, 19, 2, 20, 1, 21, 1, 22, 2, 23, 1, 24, 2, 25, 1, 26, 2, 27,
		1, 28, 1, 29, 2, 30, 2, 31, 2, 32, 1, 33, 2, 34, 3, 35, 2, 36,
		3, 37, 26, 38, 1, 37, 1, 36, 1, 35, 1, 34, 1, 33, 1, 32, 1, 31,
		1, 29, 1, 28, 1, 27, 1, 25, 1, 24, 1, 22, 1, 21, 1, 20, 1, 18,
		1, 16, 1, 15, 1, 14, 1, 13, 1, 11, 1, 10, 1, 8, 1, 7, 1, 6,
		1, 5, 1, 4, 2, 3, 1, 2, 2, 1, 4, 0, 1, 1, 1, 3, 1, 4, 1, 7,
		1, 9, 1, 12, 1, 14, 1, 17, 1, 20, 3, 22, 1, 21, 1, 22
	};
	
	private static final int[] rrectGrad = {
		205, 90, 206, 78, 207, 72, 208, 66, 209, 62, 210, 58, 211, 54,
		212, 50, 213, 46, 214, 36, 215, 30, 216, 24, 217, 12
	};
	
	private static final int[] outGrad = {
		205, 12, 202, 11, 198, 10, 192, 9, 186, 8, 180, 7,
		172, 6, 164, 5, 156, 4, 150, 3, 148, 2, 146, 1
	};
}