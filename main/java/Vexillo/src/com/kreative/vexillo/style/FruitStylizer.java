package com.kreative.vexillo.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.RenderedOp;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;

public class FruitStylizer implements Stylizer {
	private static final ImageScaler scaler = ImageScaler.ITERATIVE_BICUBIC;
	private static final int[] dims = { 280, 180, 2, 12 };
	private static final int[] border = { 20, 20, 66, 60 };
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, -0.333508303f, 1, 3.893184497e-3f, 0, 0, -9.390406971e-6f, 0, 0, 0 };
	private static final float[] scale = { 0.5f, 0.5f, 0, 0 };
	private static final int[] bevel = { 146, 151, 170, 186, 198, 211 };
	
	private static final BufferedImage rectangularTemplate;
	static {
		BufferedImage tmpl = null;
		try { tmpl = ImageIO.read(FruitStylizer.class.getResource("FruitStylizer.template.png")); }
		catch (Exception e) { e.printStackTrace(); }
		rectangularTemplate = tmpl;
	}
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int w, int h, ImageScaler unused2, int unused3, int unused4) {
		BufferedImage i = r.renderToImage(dims[0], dims[1], scaler, dims[2], 0);
		RenderedOp bi = JAIUtils.border(i, border[0], border[1], border[2], border[3], BorderExtender.BORDER_COPY);
		RenderedOp wi = JAIUtils.warp(bi, xCoeffs, yCoeffs);
		RenderedOp si = JAIUtils.scale(wi, scale[0], scale[1], scale[2], scale[3]);
		BufferedImage template = rectangularTemplate;
		if (template == null || !r.isRectangular()) {
			BufferedImage t = createTemplate(r);
			RenderedOp bt = JAIUtils.border(t, border[0], border[1], 0, 0, BorderExtender.BORDER_ZERO);
			RenderedOp wt = JAIUtils.warp(bt, xCoeffs, yCoeffs);
			RenderedOp st = JAIUtils.scale(wt, scale[0], scale[1], scale[2], scale[3]);
			template = st.getAsBufferedImage();
		}
		i = JAIUtils.multiply(template, si.getAsBufferedImage());
		return ImageScaler.ITERATIVE_BICUBIC.scale(i, w, h);
	}
	
	private static BufferedImage createTemplate(FlagRenderer r) {
		// TODO Improve fidelity to closer match iOS style.
		Shape outer = r.getBoundaryShape(0, border[2], dims[0], dims[1]);
		Shape inner = contract(outer, dims[3]);
		int w = dims[0], h = dims[1] + border[2] + border[3];
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[w];
		for (int i = 0; i < w; i++) rgb[i] = 0xFFFFFF;
		tmpl.setRGB(0, 0, w, h, rgb, 0, 0);
		Graphics2D g = tmpl.createGraphics();
		JAIUtils.prep(g);
		
		for (int i = 0; i < dims[3] / 2; i++) {
			int k = bevel[i];
			g.setPaint(new Color(k, k, k));
			g.fill(contract(outer, i * 2));
		}
		
		g.setPaint(new LinearGradientPaint(
			new Point(w/2, 0),
			new Point(w/2, h),
			new float[]{ 0, 0.2f, 0.8f, 1 },
			new Color[]{
				new Color(255, 255, 255, 25),
				new Color(255, 255, 255, 0),
				new Color(0, 0, 0, 0),
				new Color(0, 0, 0, 25),
			}
		));
		g.fill(outer);
		
		g.setPaint(new RadialGradientPaint(
			new Point(w/2, 0), w/2,
			new float[]{ 0, 0.75f, 1 },
			new Color[]{
				new Color(210, 210, 210, 255),
				new Color(210, 210, 210, 128),
				new Color(210, 210, 210, 0),
			}
		));
		g.fill(outer);
		
		g.setPaint(Color.white);
		g.fill(inner);
		
		g.setPaint(new RadialGradientPaint(
			new Point(w/2, h/2),
			Math.max(w/2, h/2),
			new float[]{ 0, 1 },
			new Color[]{
				new Color(0, 0, 0, 0),
				new Color(0, 0, 0, 25),
			}
		));
		g.fill(inner);
		
		g.setPaint(new LinearGradientPaint(
			new Point(0, h/2),
			new Point(w, h/2),
			new float[]{ 0, 0.65f, 0.85f, 0.95f, 1 },
			new Color[]{
				new Color(0, 0, 0, 25),
				new Color(0, 0, 0, 0),
				new Color(0, 0, 0, 25),
				new Color(0, 0, 0, 0),
				new Color(0, 0, 0, 25),
			}
		));
		g.fill(inner);
		
		g.dispose();
		return tmpl;
	}
	
	private static Shape contract(Shape s, int t) {
		if (t < 1) return s;
		Shape ss = new BasicStroke(t * 2).createStrokedShape(s);
		Area cs = new Area(s); cs.subtract(new Area(ss));
		return cs;
	}
}