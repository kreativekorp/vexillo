package com.kreative.vexillo.style;

import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;

public interface Stylizer {
	public BufferedImage stylize(
		FlagRenderer r,
		// These parameters may be ignored if desired.
		int width, int height, int supersample, int glaze
	);
}