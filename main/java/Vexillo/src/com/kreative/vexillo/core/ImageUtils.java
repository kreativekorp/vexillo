package com.kreative.vexillo.core;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImageUtils {
	public static BufferedImage scale(BufferedImage img, int tw, int th) {
		int w = img.getWidth();
		int h = img.getHeight();
		while (w != tw || h != th) {
			if (w > tw) {
				w /= 2; if (w < tw) w = tw;
			} else if (w < tw) {
				w *= 2; if (w > tw) w = tw;
			}
			if (h > th) {
				h /= 2; if (h < th) h = th;
			} else if (h < th) {
				h *= 2; if (h > th) h = th;
			}
			BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = tmp.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.drawImage(img, 0, 0, w, h, null);
			g.dispose();
			img = tmp;
		}
		return img;
	}
}