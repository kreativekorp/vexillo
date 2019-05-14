package com.kreative.vexillo.style;

import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;

public interface Stylizer {
	public BufferedImage stylize(
		FlagRenderer r, int width, int height,
		// The following parameters may be ignored if desired.
		int supersample, int glaze
	);
}