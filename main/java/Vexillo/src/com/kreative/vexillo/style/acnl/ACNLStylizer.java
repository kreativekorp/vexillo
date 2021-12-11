package com.kreative.vexillo.style.acnl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;
import com.kreative.vexillo.core.ImageSource;
import com.kreative.vexillo.style.Stylizer;

public class ACNLStylizer implements Stylizer {
	@Override
	public BufferedImage stylize(FlagRenderer r, int w, int h, ImageScaler s, int ss, int g) {
		BufferedImage image = createBestCard(r, s, ss, g).getCardImage();
		return ImageScaler.ITERATIVE_BICUBIC.scale(image, w, h);
	}
	
	private static ACQRCard createBestCard(FlagRenderer r, ImageScaler s, int ss, int g) {
		if (r.getFlag().images().containsKey(".acnl")) {
			for (ImageSource src : r.getFlag().images().get(".acnl")) {
				try {
					InputStream in = src.getInputStream(r.getParentFile());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					for (int b = in.read(); b >= 0; b = in.read()) out.write(b);
					in.close();
					out.close();
					return new ACNLFile(out.toByteArray()).getQRCard(0);
				} catch (IOException e) {
					continue;
				}
			}
		}
		if (ss == 42) {
			return Candidate.createCandidates(r, g).get(0).getQRCard();
		} else {
			return createCard(r, s, ss, g);
		}
	}
	
	protected static ACQRCard createCard(FlagRenderer r, ImageScaler s, int ss, int g) {
		BufferedImage image = r.renderToImage(32, 32, s, ss, g);
		int[] palette = makePalette(image);
		ACNLFile acnl = new ACNLFile();
		acnl.setTitle(getTitle(r));
		acnl.setCreatorID(0xF1A6);
		acnl.setCreatorName("Vexillo");
		acnl.setTownID(0xBECA);
		acnl.setTownName("Kreative");
		acnl.setPaletteRGB(palette);
		acnl.setImage(image);
		return acnl.getQRCard(0);
	}
	
	private static int[] makePalette(BufferedImage image) {
		int[] rp = ColorReducer.reduce(image, 15);
		int[] p = new int[15]; int i = 0;
		for (int rgb : rp) p[i++] = rgb;
		while (i < 15) p[i++] = -1;
		return p;
	}
	
	private static String getTitle(FlagRenderer r) {
		String name = r.getFlag().getName();
		if (name != null && name.length() > 0) return name;
		String id = r.getFlag().getId();
		if (id != null && id.length() > 0) return id;
		String fn = r.getFlagFile().getName();
		int i = fn.lastIndexOf('.');
		if (i > 0) fn = fn.substring(0, i);
		return fn;
	}
}