package com.kreative.vexillo.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpCubic;
import com.kreative.vexillo.core.FlagRenderer;

public class NumberStylizer implements Stylizer {
	private static final int[] dims = { 252, 172, 2, 8 };
	private static final int[] border = { 10, 10, 26, 30 };
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, 4.901103408e-1f, 1, -5.155586522e-3f, 0, 0, 1.374823072e-5f, 0, 0, 0 };
	private static final float[] scale = { 0.5f, 0.5f, 0, 0 };
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int unused1, int unused2, int unused3, int unused4) {
		Shape sh = r.getBoundaryShape(0, 0, dims[0], dims[1]);
		BufferedImage image = r.renderToImage(dims[0], dims[1], dims[2], 0);
		Graphics2D g = image.createGraphics();
		g.setClip(sh);
		g.setPaint(new Color(0, 0, 0, 57));
		g.setStroke(new BasicStroke(dims[3] * 2));
		g.draw(sh);
		g.dispose();
		RenderedOp bordered = border(image, border[0], border[1], border[2], border[3], BorderExtender.BORDER_ZERO);
		RenderedOp warped = warp(bordered, xCoeffs, yCoeffs);
		RenderedOp scaled = scale(warped, scale[0], scale[1], scale[2], scale[3]);
		return scaled.getAsBufferedImage();
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
}