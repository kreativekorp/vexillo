package com.kreative.vexillo.style;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpCubic;

public class JAIUtils {
	public static void prep(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
	
	public static RenderedOp border(Object src, int l, int r, int t, int b, int type) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(l); pb.add(r); pb.add(t); pb.add(b);
		pb.add(BorderExtender.createInstance(type));
		pb.add(null);
		return JAI.create("border", pb);
	}
	
	public static RenderedOp warp(Object src, float[] xCoeffs, float[] yCoeffs) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(new WarpCubic(xCoeffs, yCoeffs));
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		return JAI.create("warp", pb);
	}
	
	public static RenderedOp scale(Object src, float sx, float sy, float tx, float ty) {
		ParameterBlock pb = new ParameterBlock();
		pb.addSource(src);
		pb.add(sx); pb.add(sy); pb.add(tx); pb.add(ty);
		pb.add(Interpolation.getInstance(Interpolation.INTERP_BILINEAR));
		return JAI.create("scale", pb);
	}
	
	// I can't get sensible output out of JAI.create("multiply") so here's my own.
	public static BufferedImage multiply(BufferedImage im1, BufferedImage im2) {
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