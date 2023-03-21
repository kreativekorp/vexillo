package com.kreative.vexillo.style.fruit;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.jai.RenderedOp;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.style.JAIUtils;

public class FruitStylizerTest {
	public static void main(String[] args) throws IOException {
		FruitStylizer fs = new FruitStylizer();
		FlagRenderer whiteRenderer = new TestFlagRenderer(Color.white, false);
		FlagRenderer blackRenderer = new TestFlagRenderer(Color.black, false);
		BufferedImage actualWhite = fs.stylize(whiteRenderer, 160, 160, null, 0, 0);
		BufferedImage actualBlack = fs.stylize(blackRenderer, 160, 160, null, 0, 0);
		BufferedImage actualRange = diffImage(difference(actualWhite, actualBlack), 160, 160, 255 << 24);
		BufferedImage expectedWhite = ImageIO.read(FruitStylizerTest.class.getResourceAsStream("fruitstylizer-max.png"));
		BufferedImage expectedBlack = ImageIO.read(FruitStylizerTest.class.getResourceAsStream("fruitstylizer-min.png"));
		BufferedImage expectedRange = diffImage(difference(expectedWhite, expectedBlack), 160, 160, 255 << 24);
		ImageIO.write(actualWhite, "png", new File("dev/test-white.png"));
		ImageIO.write(actualBlack, "png", new File("dev/test-black.png"));
		ImageIO.write(actualRange, "png", new File("dev/test-range.png"));
		int[] whiteDiff = difference(actualWhite, expectedWhite);
		int[] blackDiff = difference(actualBlack, expectedBlack);
		int[] rangeDiff = difference(actualRange, expectedRange);
		System.out.println("white: " + diffString(whiteDiff));
		System.out.println("black: " + diffString(blackDiff));
		System.out.println("range: " + diffString(rangeDiff));
		ImageIO.write(diffImage(whiteDiff, 160, 160, -1), "png", new File("dev/test-whitediff.png"));
		ImageIO.write(diffImage(blackDiff, 160, 160, -1), "png", new File("dev/test-blackdiff.png"));
		ImageIO.write(diffImage(rangeDiff, 160, 160, -1), "png", new File("dev/test-rangediff.png"));
		
		String[] imageNames = new String[] { "max", "min", "min2", "minminuslingrad", "range" };
		for (String imageName : imageNames) {
			BufferedImage img = ImageIO.read(new File("dev/aceflag" + imageName + ".png"));
			ImageIO.write(unwarp(img), "png", new File("dev/unwarp-" + imageName + ".png"));
		}
	}
	
	private static final float[] xCoeffs = { 0, 1, 0, 0, 0, 0, 0, 0, 0, 0 };
	private static final float[] yCoeffs = { 0, 0.5005041713f, 1, -4.456608915e-3f, 0, 0, 9.390406971e-6f, 0, 0, 0 };
	private static final float[] scale = { 2f, 2f, 0, 0 };
	private static BufferedImage unwarp(BufferedImage img) {
		RenderedOp si = JAIUtils.scale(img, scale[0], scale[1], scale[2], scale[3]);
		RenderedOp wi = JAIUtils.warp(si, xCoeffs, yCoeffs);
		return wi.getAsBufferedImage();
	}
	
	private static String diffString(int[] diff) {
		long adist = 0, rdist = 0, gdist = 0, bdist = 0;
		int acount = 0, rcount = 0, gcount = 0, bcount = 0;
		int amin = 255, rmin = 255, gmin = 255, bmin = 255;
		int amax = 0, rmax = 0, gmax = 0, bmax = 0;
		for (int argb : diff) {
			adist += ((argb >> 24) & 0xFF) * ((argb >> 24) & 0xFF);
			rdist += ((argb >> 16) & 0xFF) * ((argb >> 16) & 0xFF);
			gdist += ((argb >>  8) & 0xFF) * ((argb >>  8) & 0xFF);
			bdist += ((argb >>  0) & 0xFF) * ((argb >>  0) & 0xFF);
			if (((argb >> 24) & 0xFF) != 0) acount++;
			if (((argb >> 16) & 0xFF) != 0) rcount++;
			if (((argb >>  8) & 0xFF) != 0) gcount++;
			if (((argb >>  0) & 0xFF) != 0) bcount++;
			amin = Math.min(amin, ((argb >> 24) & 0xFF));
			rmin = Math.min(rmin, ((argb >> 16) & 0xFF));
			gmin = Math.min(gmin, ((argb >>  8) & 0xFF));
			bmin = Math.min(bmin, ((argb >>  0) & 0xFF));
			amax = Math.max(amax, ((argb >> 24) & 0xFF));
			rmax = Math.max(rmax, ((argb >> 16) & 0xFF));
			gmax = Math.max(gmax, ((argb >>  8) & 0xFF));
			bmax = Math.max(bmax, ((argb >>  0) & 0xFF));
		}
		return (
			"\n\tA^2:" + adist + " R^2:" + rdist + " G^2:" + gdist + " B^2:" + bdist +
			"\n\t#A:" + acount + " #R:" + rcount + " #G:" + gcount + " #B:" + bcount +
			"\n\tAmin:" + amin + " Rmin:" + rmin + " Gmin:" + gmin + " Bmin:" + bmin +
			"\n\tAmax:" + amax + " Rmax:" + rmax + " Gmax:" + gmax + " Bmax:" + bmax
		);
	}
	
	private static BufferedImage diffImage(int[] diff, int w, int h, int mask) {
		int[] ffid = new int[diff.length];
		for (int i = 0; i < diff.length; i++) ffid[i] = diff[i] ^ mask;
		BufferedImage d = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		d.setRGB(0, 0, w, h, ffid, 0, w);
		return d;
	}
	
	private static int differenceRGB(int s1, int s2) {
		int a = Math.abs(((s1 >> 24) & 0xFF) - ((s2 >> 24) & 0xFF));
		int r = Math.abs(((s1 >> 16) & 0xFF) - ((s2 >> 16) & 0xFF));
		int g = Math.abs(((s1 >>  8) & 0xFF) - ((s2 >>  8) & 0xFF));
		int b = Math.abs(((s1 >>  0) & 0xFF) - ((s2 >>  0) & 0xFF));
		return (a << 24) | (r << 16) | (g << 8) | (b << 0);
	}
	
	private static int[] difference(BufferedImage s1, BufferedImage s2) {
		int s1w = s1.getWidth(), s2w = s2.getWidth(), dw = Math.max(s1w, s2w);
		int s1h = s1.getHeight(), s2h = s2.getHeight(), dh = Math.max(s1h, s2h);
		int[] s1rgb = new int[s1w * s1h], s2rgb = new int[s2w * s2h], drgb = new int[dw * dh];
		s1.getRGB(0, 0, s1w, s1h, s1rgb, 0, s1w); s2.getRGB(0, 0, s2w, s2h, s2rgb, 0, s2w);
		for (int s1j = 0, s2j = 0, dj = 0, y = 0; y < dh; y++, s1j += s1w, s2j += s2w, dj += dw) {
			for (int s1i = s1j, s2i = s2j, di = dj, x = 0; x < dw; x++, s1i++, s2i++, di++) {
				drgb[di] = differenceRGB(
					(y < s1h && x < s1w) ? s1rgb[s1i] : 0,
					(y < s2h && x < s2w) ? s2rgb[s2i] : 0
				);
			}
		}
		return drgb;
	}
	
	private static class TestFlagRenderer extends FlagRenderer {
		private final Color color;
		private final boolean isRectangular;
		private TestFlagRenderer(Color color, boolean isRectangular) {
			super(null, null, null);
			this.color = color;
			this.isRectangular = isRectangular;
		}
		public void render(Graphics2D g, int x, int y, int w, int h) {
			g.setColor(this.color);
			g.fillRect(x, y, w, h);
		}
		public boolean isRectangular() {
			return this.isRectangular;
		}
		public Rectangle getBoundaryRect(int x, int y, int w, int h) {
			return new Rectangle(x, y, w, h);
		}
		public Shape getBoundaryShape(int x, int y, int w, int h) {
			return new Rectangle(x, y, w, h);
		}
	}
}
