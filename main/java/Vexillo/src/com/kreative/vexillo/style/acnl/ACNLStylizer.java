package com.kreative.vexillo.style.acnl;

import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;
import com.kreative.vexillo.style.Stylizer;

public class ACNLStylizer implements Stylizer {
	@Override
	public BufferedImage stylize(FlagRenderer r, int w, int h, ImageScaler s, int ss, int g) {
		BufferedImage image;
		if (ss == 42) {
			image = Candidate.createCandidates(r, g).get(0).getQRCard().getCardImage();
		} else {
			image = createCard(r, s, ss, g).getCardImage();
		}
		return ImageScaler.ITERATIVE_BICUBIC.scale(image, w, h);
	}
	
	public ACQRCard createCard(FlagRenderer r, ImageScaler s, int ss, int g) {
		BufferedImage image = r.renderToImage(32, 32, s, ss, g);
		int[] palette = makePalette(image);
		ACNLFile acnl = new ACNLFile();
		acnl.setTitle(getTitle(r));
		acnl.setCreatorID(22136);
		acnl.setCreatorName("Vexillo");
		acnl.setTownID(19307);
		acnl.setTownName("Kreative");
		acnl.setPaletteRGB(palette);
		acnl.setImage(image);
		return acnl.getQRCard(0);
	}
	
	private int[] makePalette(BufferedImage image) {
		int[] rp = ColorReducer.reduce(image, 15);
		int[] p = new int[15]; int i = 0;
		for (int rgb : rp) p[i++] = rgb;
		while (i < 15) p[i++] = -1;
		return p;
	}
	
	private String getTitle(FlagRenderer r) {
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