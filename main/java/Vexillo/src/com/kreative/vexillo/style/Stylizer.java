package com.kreative.vexillo.style;

import java.awt.image.BufferedImage;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageScaler;

public interface Stylizer {
	public BufferedImage stylize(
		FlagRenderer r, int width, int height,
		// The following parameters may be ignored if desired.
		ImageScaler supersampler, int supersample, int glaze
	);
}