package com.kreative.vexillo.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public enum ImageScaler {
	DEFAULT(false, null, "Default"),
	NEAREST(false, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, "Nearest"),
	BILINEAR(false, RenderingHints.VALUE_INTERPOLATION_BILINEAR, "Bilinear"),
	BICUBIC(false, RenderingHints.VALUE_INTERPOLATION_BICUBIC, "Bicubic"),
	ITERATIVE_DEFAULT(true, null, "Iterative Default"),
	ITERATIVE_NEAREST(true, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR, "Iterative Nearest"),
	ITERATIVE_BILINEAR(true, RenderingHints.VALUE_INTERPOLATION_BILINEAR, "Iterative Bilinear"),
	ITERATIVE_BICUBIC(true, RenderingHints.VALUE_INTERPOLATION_BICUBIC, "Iterative Bicubic");
	
	private final boolean iterative;
	private final Object interpolation;
	private final String name;
	
	private ImageScaler(boolean iterative, Object interpolation, String name) {
		this.iterative = iterative;
		this.interpolation = interpolation;
		this.name = name;
	}
	
	public BufferedImage scale(BufferedImage image, int w, int h) {
		if (image == null) {
			return null;
		} else if (image.getWidth() == w && image.getHeight() == h) {
			return image;
		} else if (iterative) {
			return scaleIterative(image, w, h, interpolation);
		} else {
			return scaleOnce(image, w, h, interpolation);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	private static BufferedImage scaleIterative(BufferedImage image, int tw, int th, Object i) {
		int iw = image.getWidth();
		int ih = image.getHeight();
		while (iw != tw || ih != th) {
			if (iw > tw) {
				iw /= 2; if (iw < tw) iw = tw;
			} else if (iw < tw) {
				iw *= 2; if (iw > tw) iw = tw;
			}
			if (ih > th) {
				ih /= 2; if (ih < th) ih = th;
			} else if (ih < th) {
				ih *= 2; if (ih > th) ih = th;
			}
			image = scaleOnce(image, iw, ih, i);
		}
		return image;
	}
	
	private static BufferedImage scaleOnce(BufferedImage src, int w, int h, Object i) {
		BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dst.createGraphics();
		if (i != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, i);
		g.drawImage(src, 0, 0, w, h, null);
		g.dispose();
		return dst;
	}
}