package com.kreative.vexillo.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.RenderedOp;
import com.kreative.vexillo.core.FlagRenderer;

public class NumberStylizer implements Stylizer {
	private static final int[] dims = { 252, 172, 2, 8 };
	private static final int[] border = { 10, 10, 26, 30 };
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, 4.901103408e-1f, 1, -5.155586522e-3f, 0, 0, 1.374823072e-5f, 0, 0, 0 };
	private static final float[] scale = { 0.5f, 0.5f, 0, 0 };
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int unused1, int unused2, int unused3, int unused4) {
		BufferedImage i = r.renderToImage(dims[0], dims[1], dims[2], 0);
		RenderedOp bi = JAIUtils.border(i, border[0], border[1], border[2], border[3], BorderExtender.BORDER_COPY);
		RenderedOp wi = JAIUtils.warp(bi, xCoeffs, yCoeffs);
		RenderedOp si = JAIUtils.scale(wi, scale[0], scale[1], scale[2], scale[3]);
		BufferedImage t = createTemplate(r);
		RenderedOp bt = JAIUtils.border(t, border[0], border[1], 0, 0, BorderExtender.BORDER_ZERO);
		RenderedOp wt = JAIUtils.warp(bt, xCoeffs, yCoeffs);
		RenderedOp st = JAIUtils.scale(wt, scale[0], scale[1], scale[2], scale[3]);
		return JAIUtils.multiply(st.getAsBufferedImage(), si.getAsBufferedImage());
	}
	
	private static BufferedImage createTemplate(FlagRenderer r) {
		Shape outer = r.getBoundaryShape(0, border[2], dims[0], dims[1]);
		Shape stroke = new BasicStroke(dims[3] * 2).createStrokedShape(outer);
		Area inner = new Area(outer); inner.subtract(new Area(stroke));
		int w = dims[0], h = dims[1] + border[2] + border[3];
		BufferedImage tmpl = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[w];
		for (int i = 0; i < w; i++) rgb[i] = 0xFFFFFF;
		tmpl.setRGB(0, 0, w, h, rgb, 0, 0);
		Graphics2D g = tmpl.createGraphics();
		JAIUtils.prep(g);
		g.setPaint(new Color(0xC6C6C6));
		g.fill(outer);
		g.setPaint(Color.white);
		g.fill(inner);
		g.dispose();
		return tmpl;
	}
}