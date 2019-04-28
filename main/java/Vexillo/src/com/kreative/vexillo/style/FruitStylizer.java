package com.kreative.vexillo.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.imageio.ImageIO;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpCubic;
import com.kreative.vexillo.core.FlagRenderer;

public class FruitStylizer implements Stylizer {
	private static final int[] dims = { 280, 180, 2, 12 };
	private static final int[] border = { 20, 20, 66, 60 };
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, -0.333508303f, 1, 3.893184497e-3f, 0, 0, -9.390406971e-6f, 0, 0, 0 };
	private static final float[] scale = { 0.5f, 0.5f, 0, 0 };
	
	private static final BufferedImage rectangularTemplate;
	static {
		BufferedImage tmpl = null;
		try { tmpl = ImageIO.read(FruitStylizer.class.getResource("FruitStylizer.template.png")); }
		catch (Exception e) { e.printStackTrace(); }
		rectangularTemplate = tmpl;
	}
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int unused1, int unused2, int unused3, int unused4) {
		BufferedImage image = r.renderToImage(dims[0], dims[1], dims[2], 0);
		RenderedOp bordered = border(image, border[0], border[1], border[2], border[3], BorderExtender.BORDER_COPY);
		RenderedOp warped = warp(bordered, xCoeffs, yCoeffs);
		RenderedOp scaled = scale(warped, scale[0], scale[1], scale[2], scale[3]);
		BufferedImage template = rectangularTemplate;
		if (template == null || !r.isRectangular()) {
			BufferedImage tmpl = createTemplate(r, dims[0], dims[1], dims[3]);
			RenderedOp bt = border(tmpl, border[0], border[1], border[2], border[3], BorderExtender.BORDER_ZERO);
			RenderedOp wt = warp(bt, xCoeffs, yCoeffs);
			RenderedOp st = scale(wt, scale[0], scale[1], scale[2], scale[3]);
			template = st.getAsBufferedImage();
		}
		return multiply(template, scaled.getAsBufferedImage());
	}
	
	private static RenderedOp border(Object src, int l, int r, int t, int b, int type) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(l); pb.add(r); pb.add(t); pb.add(b);
		pb.add(BorderExtender.createInstance(type));
		pb.add(null);
		return JAI.create("border", pb);
	}
	
	private static RenderedOp warp(Object src, float[] xCoeffs, float[] yCoeffs) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(new WarpCubic(xCoeffs, yCoeffs));
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		return JAI.create("warp", pb);
	}
	
	private static RenderedOp scale(Object src, float sx, float sy, float tx, float ty) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(sx); pb.add(sy); pb.add(tx); pb.add(ty);
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		return JAI.create("scale", pb);
	}
	
	private static BufferedImage createTemplate(FlagRenderer r, int w, int h, int t) {
		// TODO Improve fidelity to match iOS style.
		Shape sh = r.getBoundaryShape(0, 0, w, h);
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = tmpl.createGraphics();
		g.setClip(sh);
		g.setPaint(new RadialGradientPaint(
			new Point(w/2, h/2),
			Math.max(w/2, h/2),
			new float[]{0, 1},
			new Color[]{
				Color.white,
				new Color(0xD1D1D1)
			}
		));
		g.fillRect(0, 0, w, h);
		g.setPaint(new Color(0xA8A8A8));
		g.setStroke(new BasicStroke(t * 2));
		g.draw(sh);
		g.dispose();
		return tmpl;
	}
	
	// I can't get sensible output out of JAI.create("multiply") so here's my own.
	private static BufferedImage multiply(BufferedImage im1, BufferedImage im2) {
		int w1 = im1.getWidth(), w2 = im2.getWidth(), w3 = Math.max(w1, w2);
		int h1 = im1.getHeight(), h2 = im2.getHeight(), h3 = Math.max(h1, h2);
		int[] rgb1 = new int[w1 * h1], rgb2 = new int[w2 * h2], rgb3 = new int[w3 * h3];
		im1.getRGB(0, 0, w1, h1, rgb1, 0, w1); im2.getRGB(0, 0, w2, h2, rgb2, 0, w2);
		for (int j1 = 0, j2 = 0, j3 = 0, y = 0; y < h1 && y < h2; y++, j1 += w1, j2 += w2, j3 += w3) {
			for (int i1 = j1, i2 = j2, i3 = j3, x = 0; x < w1 && x < w2; x++, i1++, i2++, i3++) {
				int a = ((rgb1[i1] >> 24) & 0xFF) * ((rgb2[i2] >> 24) & 0xFF) / 255;
				int r = ((rgb1[i1] >> 16) & 0xFF) * ((rgb2[i2] >> 16) & 0xFF) / 255;
				int g = ((rgb1[i1] >>  8) & 0xFF) * ((rgb2[i2] >>  8) & 0xFF) / 255;
				int b = ((rgb1[i1] >>  0) & 0xFF) * ((rgb2[i2] >>  0) & 0xFF) / 255;
				rgb3[i3] = (a << 24) | (r << 16) | (g << 8) | (b << 0);
			}
		}
		BufferedImage im3 = new BufferedImage(w3, h3, BufferedImage.TYPE_INT_ARGB);
		im3.setRGB(0, 0, w3, h3, rgb3, 0, w3);
		return im3;
	}
}