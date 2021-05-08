package com.kreative.vexillo.style.acnl;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;

public class Candidate implements Comparable<Candidate> {
	public static List<Candidate> createCandidates(FlagRenderer r, int glaze) {
		List<Candidate> candidates = new ArrayList<Candidate>();
		BufferedImage srcImage4x = r.renderToImage(288, 288, null, 0, glaze * 9);
		candidates.add(new Candidate(r, ImageScaler.ITERATIVE_BICUBIC, 9, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.ITERATIVE_BILINEAR, 9, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BICUBIC, 9, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BILINEAR, 9, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.ITERATIVE_BICUBIC, 3, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.ITERATIVE_BILINEAR, 3, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BICUBIC, 3, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BILINEAR, 3, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BICUBIC, 2, glaze, srcImage4x));
		candidates.add(new Candidate(r, ImageScaler.BILINEAR, 2, glaze, srcImage4x));
		candidates.add(new Candidate(r, null, 0, glaze, srcImage4x));
		Collections.sort(candidates);
		return candidates;
	}
	
	private final ACQRCard card;
	private final BufferedImage image;
	private final BufferedImage image3x;
	private final long ca;
	private final long sh;
	private final long score;
	private final String params;
	
	private Candidate(FlagRenderer r, ImageScaler s, int ss, int glaze, BufferedImage srcImage4x) {
		ACNLStylizer style = new ACNLStylizer();
		card = style.createCard(r, s, ss, glaze);
		image = card.getPreview();
		image3x = new BufferedImage(96, 96, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image3x.createGraphics();
		g.drawImage(image, 0, 0, 96, 96, null);
		g.dispose();
		ca = chromaticAberration(srcImage4x, image);
		sh = sharpness(image);
		score = ca * 80 + sh;
		params = ((s == null) ? "" : ("-i " + s.name().toLowerCase().replace('_', '-') + " ")) + "-s " + ss;
	}
	
	public ACQRCard getQRCard() { return card; }
	public BufferedImage getPreview() { return image; }
	public BufferedImage getPreview3x() { return image3x; }
	public String getParamString() { return params; }
	
	@Override
	public int compareTo(Candidate that) {
		if (this.score < that.score) return -1;
		if (this.score > that.score) return +1;
		return 0;
	}
	
	private static long chromaticAberration(BufferedImage srcImage4x, BufferedImage image) {
		long score = 0;
		int[] srcRGB = new int[288 * 288];
		int[] imgRGB = new int[32 * 32];
		srcImage4x.getRGB(0, 0, 288, 288, srcRGB, 0, 288);
		image.getRGB(0, 0, 32, 32, imgRGB, 0, 32);
		for (int i = 0, y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++, i++) {
				int ir = (imgRGB[i] < 0) ? ((imgRGB[i] >> 16) & 0xFF) : 0xFF;
				int ig = (imgRGB[i] < 0) ? ((imgRGB[i] >>  8) & 0xFF) : 0xFF;
				int ib = (imgRGB[i] < 0) ? ((imgRGB[i] >>  0) & 0xFF) : 0xFF;
				int minDiff = Integer.MAX_VALUE;
				for (int q = 0; q < 9; q++) {
					for (int p = 0; p < 9; p++) {
						int j = (y * 9 + q) * 288 + (x * 9 + p);
						int sr = (srcRGB[j] < 0) ? ((srcRGB[j] >> 16) & 0xFF) : 0xFF;
						int sg = (srcRGB[j] < 0) ? ((srcRGB[j] >>  8) & 0xFF) : 0xFF;
						int sb = (srcRGB[j] < 0) ? ((srcRGB[j] >>  0) & 0xFF) : 0xFF;
						int diff = colorDiff(sr, ir, sg, ig, sb, ib);
						if (diff < minDiff) minDiff = diff;
					}
				}
				score += minDiff;
			}
		}
		return score;
	}
	
	private static long sharpness(BufferedImage image) {
		long score = 0;
		int[] rgb = new int[32 * 32];
		image.getRGB(0, 0, 32, 32, rgb, 0, 32);
		for (int i = 0, y = 0; y < 32; y++) {
			for (int x = 0; x < 32; x++, i++) {
				if (y > 0 && x > 0) {
					int r0 = (rgb[i - 33] < 0) ? ((rgb[i - 33] >> 16) & 0xFF) : 0xFF;
					int g0 = (rgb[i - 33] < 0) ? ((rgb[i - 33] >>  8) & 0xFF) : 0xFF;
					int b0 = (rgb[i - 33] < 0) ? ((rgb[i - 33] >>  0) & 0xFF) : 0xFF;
					int r1 = (rgb[i - 32] < 0) ? ((rgb[i - 32] >> 16) & 0xFF) : 0xFF;
					int g1 = (rgb[i - 32] < 0) ? ((rgb[i - 32] >>  8) & 0xFF) : 0xFF;
					int b1 = (rgb[i - 32] < 0) ? ((rgb[i - 32] >>  0) & 0xFF) : 0xFF;
					int r2 = (rgb[i -  1] < 0) ? ((rgb[i -  1] >> 16) & 0xFF) : 0xFF;
					int g2 = (rgb[i -  1] < 0) ? ((rgb[i -  1] >>  8) & 0xFF) : 0xFF;
					int b2 = (rgb[i -  1] < 0) ? ((rgb[i -  1] >>  0) & 0xFF) : 0xFF;
					int r3 = (rgb[i -  0] < 0) ? ((rgb[i -  0] >> 16) & 0xFF) : 0xFF;
					int g3 = (rgb[i -  0] < 0) ? ((rgb[i -  0] >>  8) & 0xFF) : 0xFF;
					int b3 = (rgb[i -  0] < 0) ? ((rgb[i -  0] >>  0) & 0xFF) : 0xFF;
					int d1 = colorDiff(r1, r0, g1, g0, b1, b0);
					int d2 = colorDiff(r2, r0, g2, g0, b2, b0);
					int d3 = colorDiff(r3, r0, g3, g0, b3, b0);
					score += d1 + d2 + d3;
				}
			}
		}
		return score;
	}
	
	private static int colorDiff(int r1, int r2, int g1, int g2, int b1, int b2) {
		int dr = r1 - r2;
		int dg = g1 - g2;
		int db = b1 - b2;
		float rm = (r1 + r2) / 2f;
		float rw = (rm / 256f) + 2f;
		float gw = 4f;
		float bw = ((255f - rm) / 256f) + 2f;
		return (int)Math.round(
			rw * dr * dr +
			gw * dg * dg +
			bw * db * db
		);
	}
}