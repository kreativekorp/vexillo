package com.kreative.vexillo.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;

public class BirdStylizer implements Stylizer {
	private static final int ss = 2;
	private static final int width = 160;
	private static final int height = 116;
	private static final int corner = 16;
	private static final int alpha = 17;
	
	@Override
	public BufferedImage stylize(FlagRenderer r, int unused1, int unused2, int unused3, int unused4) {
		BufferedImage im1 = new BufferedImage(width * ss, height * ss, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = im1.createGraphics();
		prep(g);
		g.clip(new RoundRectangle2D.Float(0, 0, width * ss, height * ss, corner * ss * 2, corner * ss * 2));
		r.render(g, 0, 0, width * ss, height * ss);
		g.clip(r.getBoundaryShape(0, 0, width * ss, height * ss));
		g.setPaint(new Color(0, 0, 0, alpha));
		g.fillRect(0, 0, width * ss, height * ss);
		g.dispose();
		
		BufferedImage im2 = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
		g = im2.createGraphics();
		prep(g);
		g.drawImage(im1, 0, (width - height) / 2, width, height, null);
		g.dispose();
		return im2;
	}
	
	private void prep(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}
}