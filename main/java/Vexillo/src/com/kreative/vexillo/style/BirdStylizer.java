package com.kreative.vexillo.style;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;

public class BirdStylizer implements Stylizer {
	private static final ImageScaler scaler = ImageScaler.ITERATIVE_BICUBIC;
	private static final int width = 160;
	private static final int height = 116;
	private static final int supersample = 2;
	private static final int corner = 17;
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int w, int h, ImageScaler unused2, int unused3, int unused4) {
		BufferedImage im1 = r.renderToImage(width, height, scaler, supersample, 0);
		BufferedImage im2 = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[width * height];
		im1.getRGB(0, 0, width, height, rgb, 0, width);
		im2.setRGB(0, (width - height) / 2, width, height, rgb, 0, width);
		im1 = JAIUtils.multiply(createTemplate(r), im2);
		return ImageScaler.ITERATIVE_BICUBIC.scale(im1, w, h);
	}
	
	private static BufferedImage createTemplate(FlagRenderer r) {
		Shape outer = r.getBoundaryShape(0, (width - height) / 2, width, height);
		Shape stroke = new BasicStroke(corner * 2).createStrokedShape(outer);
		Area inner = new Area(outer); inner.subtract(new Area(stroke));
		BasicStroke s = new BasicStroke(corner * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		stroke = s.createStrokedShape(inner); inner.add(new Area(stroke));
		BufferedImage tmpl = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
		int[] rgb = new int[width];
		for (int i = 0; i < width; i++) rgb[i] = 0xEEEEEE;
		tmpl.setRGB(0, 0, width, width, rgb, 0, 0);
		Graphics2D g = tmpl.createGraphics();
		JAIUtils.prep(g);
		g.setPaint(new Color(0xEEEEEE));
		g.fill(inner);
		g.dispose();
		return tmpl;
	}
}