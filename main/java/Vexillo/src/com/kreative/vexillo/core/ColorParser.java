package com.kreative.vexillo.core;

public class ColorParser {
	public static Color parse(String model, String value) {
		model = model.trim();
		value = value.trim();
		if (model.equalsIgnoreCase("rgb")) return parseRGB(value);
		if (model.equalsIgnoreCase("hsv")) return parseHSV(value);
		if (model.equalsIgnoreCase("hsl")) return parseHSL(value);
		if (model.equalsIgnoreCase("cmyk")) return parseCMYK(value);
		if (model.equalsIgnoreCase("lab")) return parseCIELab(value);
		if (model.equalsIgnoreCase("cielab")) return parseCIELab(value);
		if (model.equalsIgnoreCase("xyy")) return parseCIExyY(value);
		if (model.equalsIgnoreCase("ciexyy")) return parseCIExyY(value);
		if (model.equalsIgnoreCase("xyz")) return parseCIEXYZ(value);
		if (model.equalsIgnoreCase("ciexyz")) return parseCIEXYZ(value);
		if (model.equalsIgnoreCase("ncs")) return parseNCS(value);
		if (model.equalsIgnoreCase("munsell")) return parseMunsell(value);
		if (model.equalsIgnoreCase("cable")) return parseCable(value);
		if (model.equalsIgnoreCase("pms")) return parsePantone(value);
		if (model.equalsIgnoreCase("pantone")) return parsePantone(value);
		throw new IllegalArgumentException("Unsupported color model: " + model);
	}
	
	private static Color parseRGB(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 3) {
			try {
				int r = Integer.parseInt(values[0]);
				int g = Integer.parseInt(values[1]);
				int b = Integer.parseInt(values[2]);
				return new Color.RGB(r, g, b);
			} catch (NumberFormatException e1) { try {
				double r = Double.parseDouble(values[0]);
				double g = Double.parseDouble(values[1]);
				double b = Double.parseDouble(values[2]);
				return new Color.RGBDecimal(r, g, b);
			} catch (NumberFormatException e2) {}}
		}
		throw new IllegalArgumentException("Invalid value for RGB: " + value);
	}
	
	private static Color parseHSV(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 3) {
			try {
				double h = Double.parseDouble(values[0]);
				double s = Double.parseDouble(values[1]);
				double v = Double.parseDouble(values[2]);
				return new Color.HSV(h, s, v);
			} catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("Invalid value for HSV: " + value);
	}
	
	private static Color parseHSL(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 3) {
			try {
				double h = Double.parseDouble(values[0]);
				double s = Double.parseDouble(values[1]);
				double l = Double.parseDouble(values[2]);
				return new Color.HSL(h, s, l);
			} catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("Invalid value for HSL: " + value);
	}
	
	private static Color parseCMYK(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 4) {
			try {
				int c = Integer.parseInt(values[0]);
				int m = Integer.parseInt(values[1]);
				int y = Integer.parseInt(values[2]);
				int k = Integer.parseInt(values[3]);
				return new Color.CMYK(c, m, y, k);
			} catch (NumberFormatException e1) { try {
				double c = Double.parseDouble(values[0]);
				double m = Double.parseDouble(values[1]);
				double y = Double.parseDouble(values[2]);
				double k = Double.parseDouble(values[3]);
				return new Color.CMYKDecimal(c, m, y, k);
			} catch (NumberFormatException e2) {}}
		}
		throw new IllegalArgumentException("Invalid value for CMYK: " + value);
	}
	
	private static Color parseCIELab(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 4) {
			try {
				String i = values[0].toUpperCase();
				Color.CIELab.Illuminant j = Color.CIELab.Illuminant.valueOf(i);
				double l = Double.parseDouble(values[1]);
				double a = Double.parseDouble(values[2]);
				double b = Double.parseDouble(values[3]);
				return new Color.CIELab(j, l, a, b);
			} catch (IllegalArgumentException e) {}
		} else if (values.length == 3) {
			try {
				double l = Double.parseDouble(values[0]);
				double a = Double.parseDouble(values[1]);
				double b = Double.parseDouble(values[2]);
				return new Color.CIELab(null, l, a, b);
			} catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("Invalid value for CIE Lab: " + value);
	}
	
	private static Color parseCIExyY(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 3) {
			try {
				double x = Double.parseDouble(values[0]);
				double y = Double.parseDouble(values[1]);
				double Y = Double.parseDouble(values[2]);
				return new Color.CIExyY(x, y, Y);
			} catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("Invalid value for CIE xyY: " + value);
	}
	
	private static Color parseCIEXYZ(String value) {
		String[] values = value.split("(\\s|,)+");
		if (values.length == 3) {
			try {
				double x = Double.parseDouble(values[0]);
				double y = Double.parseDouble(values[1]);
				double z = Double.parseDouble(values[2]);
				return new Color.CIEXYZ(x, y, z);
			} catch (NumberFormatException e) {}
		}
		throw new IllegalArgumentException("Invalid value for CIE XYZ: " + value);
	}
	
	private static Color parseNCS(String value) {
		if (value.equalsIgnoreCase("base")) return new Color.NCSBase(Color.NCSBase.Hue.WHITE);
		if (value.equalsIgnoreCase("w")) return new Color.NCSBase(Color.NCSBase.Hue.WHITE);
		if (value.equalsIgnoreCase("white")) return new Color.NCSBase(Color.NCSBase.Hue.WHITE);
		if (value.equalsIgnoreCase("k")) return new Color.NCSBase(Color.NCSBase.Hue.BLACK);
		if (value.equalsIgnoreCase("black")) return new Color.NCSBase(Color.NCSBase.Hue.BLACK);
		if (value.equalsIgnoreCase("r")) return new Color.NCSBase(Color.NCSBase.Hue.RED);
		if (value.equalsIgnoreCase("red")) return new Color.NCSBase(Color.NCSBase.Hue.RED);
		if (value.equalsIgnoreCase("y")) return new Color.NCSBase(Color.NCSBase.Hue.YELLOW);
		if (value.equalsIgnoreCase("yellow")) return new Color.NCSBase(Color.NCSBase.Hue.YELLOW);
		if (value.equalsIgnoreCase("g")) return new Color.NCSBase(Color.NCSBase.Hue.GREEN);
		if (value.equalsIgnoreCase("green")) return new Color.NCSBase(Color.NCSBase.Hue.GREEN);
		if (value.equalsIgnoreCase("b")) return new Color.NCSBase(Color.NCSBase.Hue.BLUE);
		if (value.equalsIgnoreCase("blue")) return new Color.NCSBase(Color.NCSBase.Hue.BLUE);
		String[] values = value.split("(\\s|,|-)+");
		if (values.length == 3 && values[2].length() > 2) {
			try {
				int k = Integer.parseInt(values[0]);
				int s = Integer.parseInt(values[1]);
				Color.NCS.Hue h1;
				switch (values[2].charAt(0)) {
					case 'R': case 'r': h1 = Color.NCS.Hue.RED; break;
					case 'Y': case 'y': h1 = Color.NCS.Hue.YELLOW; break;
					case 'G': case 'g': h1 = Color.NCS.Hue.GREEN; break;
					case 'B': case 'b': h1 = Color.NCS.Hue.BLUE; break;
					default: throw new IllegalArgumentException();
				}
				int l = values[2].length() - 1;
				int h = Integer.parseInt(values[2].substring(1, l));
				Color.NCS.Hue h2;
				switch (values[2].charAt(l)) {
					case 'R': case 'r': h2 = Color.NCS.Hue.RED; break;
					case 'Y': case 'y': h2 = Color.NCS.Hue.YELLOW; break;
					case 'G': case 'g': h2 = Color.NCS.Hue.GREEN; break;
					case 'B': case 'b': h2 = Color.NCS.Hue.BLUE; break;
					default: throw new IllegalArgumentException();
				}
				return new Color.NCS(k, s, h1, h, h2);
			} catch (IllegalArgumentException e) {}
		}
		throw new IllegalArgumentException("Invalid value for NCS: " + value);
	}
	
	private static Color parseMunsell(String value) {
		if (value.startsWith("N") || value.startsWith("n")) {
			try {
				double v = Double.parseDouble(value.substring(1).trim());
				return new Color.MunsellNeutral(v);
			} catch (NumberFormatException e) {}
		} else {
			String[] values = value.split("(\\s|,|/)+");
			if (values.length == 3) {
				try {
					String v0 = values[0].toUpperCase();
					Color.Munsell.Hue hh;
					/**/ if (v0.endsWith("PB")) { hh = Color.Munsell.Hue.PURPLE_BLUE; v0 = v0.substring(0, v0.length()-2); }
					else if (v0.endsWith("BG")) { hh = Color.Munsell.Hue.BLUE_GREEN; v0 = v0.substring(0, v0.length()-2); }
					else if (v0.endsWith("GY")) { hh = Color.Munsell.Hue.GREEN_YELLOW; v0 = v0.substring(0, v0.length()-2); }
					else if (v0.endsWith("YR")) { hh = Color.Munsell.Hue.YELLOW_RED; v0 = v0.substring(0, v0.length()-2); }
					else if (v0.endsWith("RP")) { hh = Color.Munsell.Hue.RED_PURPLE; v0 = v0.substring(0, v0.length()-2); }
					else if (v0.endsWith("P")) { hh = Color.Munsell.Hue.PURPLE; v0 = v0.substring(0, v0.length()-1); }
					else if (v0.endsWith("B")) { hh = Color.Munsell.Hue.BLUE; v0 = v0.substring(0, v0.length()-1); }
					else if (v0.endsWith("G")) { hh = Color.Munsell.Hue.GREEN; v0 = v0.substring(0, v0.length()-1); }
					else if (v0.endsWith("Y")) { hh = Color.Munsell.Hue.YELLOW; v0 = v0.substring(0, v0.length()-1); }
					else if (v0.endsWith("R")) { hh = Color.Munsell.Hue.RED; v0 = v0.substring(0, v0.length()-1); }
					else throw new IllegalArgumentException();
					double h = Double.parseDouble(v0);
					double v = Double.parseDouble(values[1]);
					double s = Double.parseDouble(values[2]);
					return new Color.Munsell(h, hh, v, s);
				} catch (IllegalArgumentException e) {}
			}
		}
		throw new IllegalArgumentException("Invalid value for Munsell: " + value);
	}
	
	private static Color parseCable(String value) {
		try {
			return new Color.Cable(Integer.parseInt(value));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid value for Cable: " + value);
		}
	}
	
	private static Color parsePantone(String value) {
		return new Color.Pantone(value);
	}
}