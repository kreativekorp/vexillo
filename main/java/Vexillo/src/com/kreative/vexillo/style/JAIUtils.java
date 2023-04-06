package com.kreative.vexillo.style;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpGeneralPolynomial;

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
		pb.add(new WarpGeneralPolynomial(xCoeffs, yCoeffs));
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
	public static BufferedImage multiply(BufferedImage s1, BufferedImage s2) {
		int s1w = s1.getWidth(), s2w = s2.getWidth(), dw = Math.max(s1w, s2w);
		int s1h = s1.getHeight(), s2h = s2.getHeight(), dh = Math.max(s1h, s2h);
		int[] s1rgb = new int[s1w * s1h], s2rgb = new int[s2w * s2h], drgb = new int[dw * dh];
		s1.getRGB(0, 0, s1w, s1h, s1rgb, 0, s1w); s2.getRGB(0, 0, s2w, s2h, s2rgb, 0, s2w);
		for (int s1j = 0, s2j = 0, dj = 0, y = 0; y < s1h && y < s2h; y++, s1j += s1w, s2j += s2w, dj += dw) {
			for (int s1i = s1j, s2i = s2j, di = dj, x = 0; x < s1w && x < s2w; x++, s1i++, s2i++, di++) {
				int a = ((s1rgb[s1i] >> 24) & 0xFF) * ((s2rgb[s2i] >> 24) & 0xFF) / 255;
				int r = ((s1rgb[s1i] >> 16) & 0xFF) * ((s2rgb[s2i] >> 16) & 0xFF) / 255;
				int g = ((s1rgb[s1i] >>  8) & 0xFF) * ((s2rgb[s2i] >>  8) & 0xFF) / 255;
				int b = ((s1rgb[s1i] >>  0) & 0xFF) * ((s2rgb[s2i] >>  0) & 0xFF) / 255;
				drgb[di] = (a << 24) | (r << 16) | (g << 8) | (b << 0);
			}
		}
		BufferedImage d = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB);
		d.setRGB(0, 0, dw, dh, drgb, 0, dw);
		return d;
	}
	
	private static int multiplyAddRGB(int m1, int m2, int base) {
		int a = (((m1 >> 24) & 0xFF) * ((m2 >> 24) & 0xFF) / 255), ba = ((base >> 24) & 0xFF);
		int r = Math.min(255, (((m1 >> 16) & 0xFF) * ((m2 >> 16) & 0xFF) / 255) + (((base >> 16) & 0xFF) * ba / 255));
		int g = Math.min(255, (((m1 >>  8) & 0xFF) * ((m2 >>  8) & 0xFF) / 255) + (((base >>  8) & 0xFF) * ba / 255));
		int b = Math.min(255, (((m1 >>  0) & 0xFF) * ((m2 >>  0) & 0xFF) / 255) + (((base >>  0) & 0xFF) * ba / 255));
		return (a << 24) | (r << 16) | (g << 8) | (b << 0);
	}
	
	public static BufferedImage multiplyAdd(BufferedImage m1, BufferedImage m2, BufferedImage base) {
		int m1w = m1.getWidth(), m2w = m2.getWidth(), bw = base.getWidth(), dw = Math.max(Math.max(m1w, m2w), bw);
		int m1h = m1.getHeight(), m2h = m2.getHeight(), bh = base.getHeight(), dh = Math.max(Math.max(m1h, m2h), bh);
		int[] m1rgb = new int[m1w * m1h], m2rgb = new int[m2w * m2h], brgb = new int[bw * bh], drgb = new int[dw * dh];
		m1.getRGB(0, 0, m1w, m1h, m1rgb, 0, m1w); m2.getRGB(0, 0, m2w, m2h, m2rgb, 0, m2w); base.getRGB(0, 0, bw, bh, brgb, 0, bw);
		for (int m1j = 0, m2j = 0, bj = 0, dj = 0, y = 0; y < dh; y++, m1j += m1w, m2j += m2w, bj += bw, dj += dw) {
			for (int m1i = m1j, m2i = m2j, bi = bj, di = dj, x = 0; x < dw; x++, m1i++, m2i++, bi++, di++) {
				drgb[di] = multiplyAddRGB(
					(y < m1h && x < m1w) ? m1rgb[m1i] : 0,
					(y < m2h && x < m2w) ? m2rgb[m2i] : 0,
					(y < bh && x < bw) ? brgb[bi] : 0
				);
			}
		}
		BufferedImage d = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB);
		d.setRGB(0, 0, dw, dh, drgb, 0, dw);
		return d;
	}
	
	private static int maxMinRGB(int src, int max, int min) {
		int a = (((src >> 24) & 0xFF) * (((max >> 24) & 0xFF) - ((min >> 24) & 0xFF)) / 255) + ((min >> 24) & 0xFF);
		int r = (((src >> 16) & 0xFF) * (((max >> 16) & 0xFF) - ((min >> 16) & 0xFF)) / 255) + ((min >> 16) & 0xFF);
		int g = (((src >>  8) & 0xFF) * (((max >>  8) & 0xFF) - ((min >>  8) & 0xFF)) / 255) + ((min >>  8) & 0xFF);
		int b = (((src >>  0) & 0xFF) * (((max >>  0) & 0xFF) - ((min >>  0) & 0xFF)) / 255) + ((min >>  0) & 0xFF);
		return (a << 24) | (r << 16) | (g << 8) | (b << 0);
	}
	
	public static BufferedImage maxMin(BufferedImage src, BufferedImage max, BufferedImage min) {
		int sw = src.getWidth(), maxw = max.getWidth(), minw = min.getWidth(), dw = Math.max(sw, Math.max(maxw, minw));
		int sh = src.getHeight(), maxh = max.getHeight(), minh = min.getHeight(), dh = Math.max(sh, Math.max(maxh, minh));
		int[] srgb = new int[sw * sh], maxrgb = new int[maxw * maxh], minrgb = new int[minw * minh], drgb = new int[dw * dh];
		src.getRGB(0, 0, sw, sh, srgb, 0, sw); max.getRGB(0, 0, maxw, maxh, maxrgb, 0, maxw); min.getRGB(0, 0, minw, minh, minrgb, 0, minw);
		for (int sj = 0, maxj = 0, minj = 0, dj = 0, y = 0; y < dh; y++, sj += sw, maxj += maxw, minj += minw, dj += dw) {
			for (int si = sj, maxi = maxj, mini = minj, di = dj, x = 0; x < dw; x++, si++, maxi++, mini++, di++) {
				drgb[di] = maxMinRGB(
					(y < sh && x < sw) ? srgb[si] : 0,
					(y < maxh && x < maxw) ? maxrgb[maxi] : -1,
					(y < minh && x < minw) ? minrgb[mini] : 0
				);
			}
		}
		BufferedImage d = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_ARGB);
		d.setRGB(0, 0, dw, dh, drgb, 0, dw);
		return d;
	}
}