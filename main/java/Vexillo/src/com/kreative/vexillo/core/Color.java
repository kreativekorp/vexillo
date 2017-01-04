package com.kreative.vexillo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class Color {
	public interface HasRGB {
		public int getRGB();
	}
	
	public static final class Multi extends Color implements HasRGB, List<Color> {
		private final List<Color> list = new ArrayList<Color>();
		@Override
		public int getRGB() {
			for (Color color : list) {
				if (color instanceof HasRGB) {
					return ((HasRGB)color).getRGB();
				}
			}
			return 0;
		}
		@Override public boolean add(Color c) { return list.add(c); }
		@Override public void add(int i, Color c) { list.add(i, c); }
		@Override public boolean addAll(Collection<? extends Color> c) { return list.addAll(c); }
		@Override public boolean addAll(int i, Collection<? extends Color> c) { return list.addAll(i, c); }
		@Override public void clear() { list.clear(); }
		@Override public boolean contains(Object c) { return list.contains(c); }
		@Override public boolean containsAll(Collection<?> c) { return list.containsAll(c); }
		@Override public Color get(int i) { return list.get(i); }
		@Override public int indexOf(Object c) { return list.indexOf(c); }
		@Override public boolean isEmpty() { return list.isEmpty(); }
		@Override public Iterator<Color> iterator() { return list.iterator(); }
		@Override public int lastIndexOf(Object c) { return list.lastIndexOf(c); }
		@Override public ListIterator<Color> listIterator() { return list.listIterator(); }
		@Override public ListIterator<Color> listIterator(int i) { return list.listIterator(i); }
		@Override public boolean remove(Object c) { return list.remove(c); }
		@Override public Color remove(int i) { return list.remove(i); }
		@Override public boolean removeAll(Collection<?> c) { return list.removeAll(c); }
		@Override public boolean retainAll(Collection<?> c) { return list.retainAll(c); }
		@Override public Color set(int i, Color c) { return list.set(i, c); }
		@Override public int size() { return list.size(); }
		@Override public List<Color> subList(int i, int j) { return list.subList(i, j); }
		@Override public Object[] toArray() { return list.toArray(); }
		@Override public <T> T[] toArray(T[] a) { return list.toArray(a); }
	}
	
	public static final class RGB extends Color implements HasRGB {
		public final int r, g, b;
		public RGB(int r, int g, int b) {
			this.r = Math.max(0, Math.min(255, r));
			this.g = Math.max(0, Math.min(255, g));
			this.b = Math.max(0, Math.min(255, b));
		}
		@Override
		public int getRGB() {
			return 0xFF000000 | (r << 16) | (g << 8) | b;
		}
	}
	
	public static final class RGBDecimal extends Color implements HasRGB {
		public final double r, g, b;
		public RGBDecimal(double r, double g, double b) {
			this.r = Math.max(0.0, Math.min(1.0, r));
			this.g = Math.max(0.0, Math.min(1.0, g));
			this.b = Math.max(0.0, Math.min(1.0, b));
		}
		@Override
		public int getRGB() {
			int r = (int)Math.round(this.r * 255);
			int g = (int)Math.round(this.g * 255);
			int b = (int)Math.round(this.b * 255);
			return 0xFF000000 | (r << 16) | (g << 8) | b;
		}
	}
	
	public static final class HSV extends Color {
		public final double h, s, v;
		public HSV(double h, double s, double v) {
			this.h = Math.max(0.0, Math.min(360.0, h));
			this.s = Math.max(0.0, Math.min(100.0, s));
			this.v = Math.max(0.0, Math.min(100.0, v));
		}
	}
	
	public static final class HSL extends Color {
		public final double h, s, l;
		public HSL(double h, double s, double l) {
			this.h = Math.max(0.0, Math.min(360.0, h));
			this.s = Math.max(0.0, Math.min(100.0, s));
			this.l = Math.max(0.0, Math.min(100.0, l));
		}
	}
	
	public static final class CMYK extends Color {
		public final int c, m, y, k;
		public CMYK(int c, int m, int y, int k) {
			this.c = Math.max(0, Math.min(100, c));
			this.m = Math.max(0, Math.min(100, m));
			this.y = Math.max(0, Math.min(100, y));
			this.k = Math.max(0, Math.min(100, k));
		}
	}
	
	public static final class CMYKDecimal extends Color {
		public final double c, m, y, k;
		public CMYKDecimal(double c, double m, double y, double k) {
			this.c = Math.max(0.0, Math.min(1.0, c));
			this.m = Math.max(0.0, Math.min(1.0, m));
			this.y = Math.max(0.0, Math.min(1.0, y));
			this.k = Math.max(0.0, Math.min(1.0, k));
		}
	}
	
	public static final class CIELab extends Color {
		public static enum Illuminant {
			A, B, C, D50, D55, D65, D75, E,
			F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12;
		}
		public final Illuminant illuminant;
		public final double l, a, b;
		public CIELab(Illuminant illuminant, double l, double a, double b) {
			this.illuminant = illuminant;
			this.l = Math.max(0.0, Math.min(100.0, l));
			this.a = Math.max(-100.0, Math.min(100.0, a));
			this.b = Math.max(-100.0, Math.min(100.0, b));
		}
	}
	
	public static final class CIExyY extends Color {
		public final double x, y, Y;
		public CIExyY(double x, double y, double Y) {
			this.x = x;
			this.y = y;
			this.Y = Y;
		}
	}
	
	public static final class CIEXYZ extends Color {
		public final double x, y, z;
		public CIEXYZ(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static final class NCSBase extends Color {
		public static enum Hue { WHITE, BLACK, RED, YELLOW, GREEN, BLUE }
		public final Hue hue;
		public NCSBase(Hue hue) {
			this.hue = hue;
		}
	}
	
	public static final class NCS extends Color {
		public static enum Hue { RED, YELLOW, GREEN, BLUE }
		public final int blackness;
		public final int chromaticity;
		public final int whiteness;
		public final Hue baseHue;
		public final int baseHuePercent;
		public final int mixedHuePercent;
		public final Hue mixedHue;
		public NCS(int k, int s, Hue h1, int h, Hue h2) {
			this.blackness = Math.max(0, Math.min(100, k));
			this.chromaticity = Math.max(0, Math.min(100, s));
			this.baseHue = h1;
			this.mixedHuePercent = Math.max(0, Math.min(100, h));
			this.mixedHue = h2;
			this.whiteness = 100 - this.blackness - this.chromaticity;
			this.baseHuePercent = 100 - this.mixedHuePercent;
		}
	}
	
	public static final class MunsellNeutral extends Color {
		public final double value;
		public MunsellNeutral(double value) {
			this.value = Math.max(0.0, Math.min(100.0, value));
		}
	}
	
	public static final class Munsell extends Color {
		public static enum Hue {
			RED, YELLOW_RED, YELLOW, GREEN_YELLOW, GREEN,
			BLUE_GREEN, BLUE, PURPLE_BLUE, PURPLE, RED_PURPLE;
		}
		public final double hueSubstep;
		public final Hue hue;
		public final double value;
		public final double chroma;
		public Munsell(double h, Hue hh, double v, double s) {
			this.hueSubstep = Math.max(0.0, Math.min(100.0, h));
			this.hue = hh;
			this.value = Math.max(0.0, Math.min(100.0, v));
			this.chroma = Math.max(0.0, Math.min(100.0, s));
		}
	}
	
	public static final class Cable extends Color {
		public final int number;
		public Cable(int number) {
			this.number = Math.max(10000, Math.min(99999, number));
		}
	}
	
	public static final class Pantone extends Color {
		public final String name;
		public Pantone(String name) {
			this.name = name.trim();
		}
	}
}