package com.kreative.vexillo.style.acnl;

import java.awt.image.BufferedImage;

public class ACNLFile {
	private byte[] data;
	
	public ACNLFile() {
		this(CANVAS);
	}
	
	public ACNLFile(int format) {
		setFormat(format);
		setUnknownC(0x0A);
		for (int i = 0; i < 15; i++) {
			int index = ((i << 4) | 0xF);
			setPaletteGlobalIndex(i, index);
		}
		setTownName("Unknown");
		setCreatorName("Unknown");
		setTitle("Design Pattern");
	}
	
	public ACNLFile(byte[] data) {
		this.data = data;
	}
	
	public String getTitle() {
		return getUTF16(0x00, 0x2A);
	}
	
	public void setTitle(String title) {
		setUTF16(0x00, 0x2A, title);
	}
	
	public int getCreatorID() {
		return getInt16(0x2A);
	}
	
	public void setCreatorID(int id) {
		setInt16(0x2A, id);
	}
	
	public String getCreatorName() {
		return getUTF16(0x2C, 0x3E);
	}
	
	public void setCreatorName(String name) {
		setUTF16(0x2C, 0x3E, name);
	}
	
	public int getCreatorGender() {
		return data[0x3E] & 0xFF;
	}
	
	public void setCreatorGender(int value) {
		data[0x3E] = (byte)value;
	}
	
	public int getCreatorReserved() {
		return data[0x3F] & 0xFF;
	}
	
	public void setCreatorReserved(int value) {
		data[0x3F] = (byte)value;
	}
	
	public int getTownID() {
		return getInt16(0x40);
	}
	
	public void setTownID(int id) {
		setInt16(0x40, id);
	}
	
	public String getTownName() {
		return getUTF16(0x42, 0x54);
	}
	
	public void setTownName(String name) {
		setUTF16(0x42, 0x54, name);
	}
	
	public int getTownLanguage() {
		return data[0x54] & 0xFF;
	}
	
	public void setTownLanguage(int value) {
		data[0x54] = (byte)value;
	}
	
	public int getTownReserved() {
		return data[0x55] & 0xFF;
	}
	
	public void setTownReserved(int value) {
		data[0x55] = (byte)value;
	}
	
	public int getTownCountry() {
		return data[0x56] & 0xFF;
	}
	
	public void setTownCountry(int value) {
		data[0x56] = (byte)value;
	}
	
	public int getTownRegion() {
		return data[0x57] & 0xFF;
	}
	
	public void setTownRegion(int value) {
		data[0x57] = (byte)value;
	}
	
	public int getPaletteGlobalIndex(int i) {
		if ((i &= 0xF) == 0xF) return -1;
		return data[0x58 + i] & 0xFF;
	}
	
	public void setPaletteGlobalIndex(int i, int index) {
		if ((i &= 0xF) == 0xF) return;
		data[0x58 + i] = (byte)index;
	}
	
	public int getPaletteRGB(int i) {
		return getGlobalColorRGB(getPaletteGlobalIndex(i));
	}
	
	public void setPaletteRGB(int i, int rgb) {
		setPaletteGlobalIndex(i, getGlobalColorIndex(rgb));
	}
	
	public int[] getPaletteGlobalIndex(int[] idx) {
		if (idx == null || idx.length < 15) {
			idx = new int[15];
		}
		for (int i = 0; i < 15; i++) {
			idx[i] = getPaletteGlobalIndex(i);
		}
		return idx;
	}
	
	public void setPaletteGlobalIndex(int[] idx) {
		for (int i = 0; i < 15; i++) {
			setPaletteGlobalIndex(i, idx[i]);
		}
	}
	
	public int[] getPaletteRGB(int[] rgb) {
		if (rgb == null || rgb.length < 15) {
			rgb = new int[15];
		}
		for (int i = 0; i < 15; i++) {
			rgb[i] = getPaletteRGB(i);
		}
		return rgb;
	}
	
	public void setPaletteRGB(int[] rgb) {
		for (int i = 0; i < 15; i++) {
			setPaletteRGB(i, rgb[i]);
		}
	}
	
	public int getLocalColorRGB(int i) {
		return getPaletteRGB(i);
	}
	
	public int getLocalColorIndex(int srgb) {
		int index = -1;
		if (srgb < 0) {
			int sr = (srgb >> 16) & 0xFF;
			int sg = (srgb >>  8) & 0xFF;
			int sb = (srgb >>  0) & 0xFF;
			int diff = Integer.MAX_VALUE;
			for (int i = 0; i < 15; i++) {
				int drgb = getPaletteRGB(i);
				if (drgb < 0) {
					int dr = (drgb >> 16) & 0xFF;
					int dg = (drgb >>  8) & 0xFF;
					int db = (drgb >>  0) & 0xFF;
					int d = colorDiff(sr, dr, sg, dg, sb, db);
					if (d < diff) {
						index = i;
						diff = d;
					}
				}
			}
		}
		return index;
	}
	
	public int getUnknownB() {
		return data[0x67] & 0xFF;
	}
	
	public void setUnknownB(int b) {
		data[0x67] = (byte)b;
	}
	
	public int getUnknownC() {
		return data[0x68] & 0xFF;
	}
	
	public void setUnknownC(int c) {
		data[0x68] = (byte)c;
	}
	
	public int getFormat() {
		return data[0x69] & 0xFF;
	}
	
	public int getWidth() {
		return getWidth(getFormat());
	}
	
	public int getHeight() {
		return getHeight(getFormat());
	}
	
	public int getFrames() {
		return getFrames(getFormat());
	}
	
	public int getLength() {
		return getLength(getFormat());
	}
	
	public void setFormat(int format) {
		int newLength = getLength(format);
		if (data == null) {
			data = new byte[newLength];
		} else if (data.length != newLength) {
			byte[] newData = new byte[newLength];
			for (int i = 0; i < data.length && i < newLength; i++) {
				newData[i] = data[i];
			}
			data = newData;
		}
		data[0x69] = (byte)format;
	}
	
	public int getUnknownD() {
		return getInt16(0x6A);
	}
	
	public void setUnknownD(int d) {
		setInt16(0x6A, d);
	}
	
	public int[] getFrameRGB(int frame) {
		int[] rgb = new int[1024];
		int[] palette = getPaletteRGB(new int[16]);
		for (int dy = ((frame << 9) | 0x6C), py = 0, y = 0; y < 32; y++, py += 32, dy += 16) {
			for (int dx = dy, px = py, x = 0; x < 32; x += 2, px += 2, dx++) {
				int b = data[dx] & 0xFF;
				rgb[px + 0] = palette[b & 15];
				rgb[px + 1] = palette[b >> 4];
			}
		}
		return rgb;
	}
	
	public void setFrameRGB(int frame, int[] rgb) {
		for (int dy = ((frame << 9) | 0x6C), py = 0, y = 0; y < 32; y++, py += 32, dy += 16) {
			for (int dx = dy, px = py, x = 0; x < 32; x += 2, px += 2, dx++) {
				int p0 = getLocalColorIndex(rgb[px + 0]) & 0xF;
				int p1 = getLocalColorIndex(rgb[px + 1]) & 0xF;
				data[dx] = (byte)(p0 | (p1 << 4));
			}
		}
	}
	
	public BufferedImage getFrameImage(int frame) {
		BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
		image.setRGB(0, 0, 32, 32, getFrameRGB(frame), 0, 32);
		return image;
	}
	
	public void setFrameImage(int frame, BufferedImage image) {
		int[] rgb = new int[1024];
		image.getRGB(0, 0, 32, 32, rgb, 0, 32);
		setFrameRGB(frame, rgb);
	}
	
	public BufferedImage getImage() {
		int x = 0, y = 0, w = getWidth(), h = getHeight();
		int i = 0, n = getFrames();
		BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		while (i < n) {
			int[] rgb = getFrameRGB(i);
			image.setRGB(x, y, 32, 32, rgb, 0, 32);
			i++;
			y += 32;
			if (y >= h) {
				y = 0;
				x += 32;
			}
		}
		return image;
	}
	
	public void setImage(BufferedImage image) {
		int x = 0, y = 0, h = image.getHeight();
		int i = 0, n = getFrames();
		while (i < n) {
			int[] rgb = new int[1024];
			image.getRGB(x, y, 32, 32, rgb, 0, 32);
			setFrameRGB(i, rgb);
			i++;
			y += 32;
			if (y >= h) {
				y = 0;
				x += 32;
			}
		}
	}
	
	public int getQRParity() {
		int p = 0;
		for (byte b : data) p ^= b;
		return p & 0xFF;
	}
	
	public byte[] getQRData(int frame) {
		int qrlen = getLength() / getFrames();
		byte[] qrdata = new byte[qrlen];
		for (int si = qrlen * frame, di = 0; di < qrlen; di++, si++) {
			qrdata[di] = data[si];
		}
		return qrdata;
	}
	
	public ACQRCode getQRCode(int frame) {
		return new ACQRCode(frame, getFrames(), getQRParity(), getQRData(frame));
	}
	
	public ACQRCard getQRCard(int frame) {
		return new ACQRCard(getTitle(), getFrameImage(frame), 3, getQRCode(frame));
	}
	
	// -- -- -- -- -- -- -- -- PRIVATE API -- -- -- -- -- -- -- -- //
	
	private int getInt16(int a) {
		int lsb = data[a + 0] & 0xFF;
		int msb = data[a + 1] & 0xFF;
		return (msb << 8) | (lsb << 0);
	}
	
	private void setInt16(int a, int v) {
		data[a + 0] = (byte)(v >> 0);
		data[a + 1] = (byte)(v >> 8);
	}
	
	private String getUTF16(int a, int b) {
		StringBuffer s = new StringBuffer();
		while (a < b) {
			int c = getInt16(a);
			if (c == 0) break;
			s.append((char)c);
			a += 2;
		}
		return s.toString();
	}
	
	private void setUTF16(int a, int b, String s) {
		char[] sa = s.toCharArray();
		int si = 0, sn = sa.length;
		while (a < b) {
			int c = (si < sn) ? sa[si++] : 0;
			setInt16(a, c);
			a += 2;
		}
	}
	
	// -- -- -- -- -- -- -- -- STATIC API -- -- -- -- -- -- -- -- //
	
	public static final int DRESS_LONG_SLEEVE = 0;
	public static final int DRESS_SHORT_SLEEVE = 1;
	public static final int DRESS_SLEEVELESS = 2;
	public static final int SHIRT_LONG_SLEEVE = 3;
	public static final int SHIRT_SHORT_SLEEVE = 4;
	public static final int SHIRT_SLEEVELESS = 5;
	public static final int HAT_HORNED = 6;
	public static final int HAT_HORNLESS = 7;
	public static final int STANDEE = 8;
	public static final int CANVAS = 9;
	
	public static int getWidth(int format) {
		return (format < 6 || format == 8) ? 64 : 32;
	}
	
	public static int getHeight(int format) {
		return (format < 6 || format == 8) ? 64 : 32;
	}
	
	public static int getFrames(int format) {
		return (format < 6 || format == 8) ? 4 : 1;
	}
	
	public static int getLength(int format) {
		return (format < 6 || format == 8) ? 2160 : 620;
	}
	
	public static int getGlobalColorRGB(int index) {
		return PALETTE[index & 0xFF];
	}
	
	public static int getGlobalColorIndex(int srgb) {
		int index = -1;
		if (srgb < 0) {
			int sr = (srgb >> 16) & 0xFF;
			int sg = (srgb >>  8) & 0xFF;
			int sb = (srgb >>  0) & 0xFF;
			int diff = Integer.MAX_VALUE;
			for (int i = 0; i < 256; i++) {
				int drgb = PALETTE[i];
				if (drgb < 0) {
					int dr = (drgb >> 16) & 0xFF;
					int dg = (drgb >>  8) & 0xFF;
					int db = (drgb >>  0) & 0xFF;
					int d = colorDiff(sr, dr, sg, dg, sb, db);
					if (d < diff) {
						index = i;
						diff = d;
					}
				}
			}
		}
		return index;
	}
	
	private static final int colorDiff(int r1, int r2, int g1, int g2, int b1, int b2) {
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
	
	private static final int[] PALETTE = {
		0xFFFFEEFF, 0xFFFF99AA, 0xFFEE5599, 0xFFFF66AA, 0xFFFF0066, 0xFFBB4477,
		0xFFCC0055, 0xFF990033, 0xFF552233, 0,  0,  0,  0,  0,  0,  0xFFFFFFFF,
		0xFFFFBBCC, 0xFFFF7777, 0xFFDD3311, 0xFFFF5544, 0xFFFF0000, 0xFFCC6666,
		0xFFBB4444, 0xFFBB0000, 0xFF882222, 0,  0,  0,  0,  0,  0,  0xFFEEEEEE,
		0xFFDDCCBB, 0xFFFFCC66, 0xFFDD6622, 0xFFFFAA22, 0xFFFF6600, 0xFFBB8855,
		0xFFDD4400, 0xFFBB4400, 0xFF663311, 0,  0,  0,  0,  0,  0,  0xFFDDDDDD,
		0xFFFFEEDD, 0xFFFFDDCC, 0xFFFFCCAA, 0xFFFFBB88, 0xFFFFAA88, 0xFFDD8866,
		0xFFBB6644, 0xFF995533, 0xFF884422, 0,  0,  0,  0,  0,  0,  0xFFCCCCCC,
		0xFFFFCCFF, 0xFFEE88FF, 0xFFCC66DD, 0xFFBB88CC, 0xFFCC00FF, 0xFF996699,
		0xFF8800AA, 0xFF550077, 0xFF330044, 0,  0,  0,  0,  0,  0,  0xFFBBBBBB,
		0xFFFFBBFF, 0xFFFF99FF, 0xFFDD22BB, 0xFFFF55EE, 0xFFFF00CC, 0xFF885577,
		0xFFBB0099, 0xFF880066, 0xFF550044, 0,  0,  0,  0,  0,  0,  0xFFAAAAAA,
		0xFFDDBB99, 0xFFCCAA77, 0xFF774433, 0xFFAA7744, 0xFF993300, 0xFF773322,
		0xFF552200, 0xFF331100, 0xFF221100, 0,  0,  0,  0,  0,  0,  0xFF999999,
		0xFFFFFFCC, 0xFFFFFF77, 0xFFDDDD22, 0xFFFFFF00, 0xFFFFDD00, 0xFFCCAA00,
		0xFF999900, 0xFF887700, 0xFF555500, 0,  0,  0,  0,  0,  0,  0xFF888888,
		0xFFDDBBFF, 0xFFBB99EE, 0xFF6633CC, 0xFF9955FF, 0xFF6600FF, 0xFF554488,
		0xFF440099, 0xFF220066, 0xFF221133, 0,  0,  0,  0,  0,  0,  0xFF777777,
		0xFFBBBBFF, 0xFF8899FF, 0xFF3333AA, 0xFF3355EE, 0xFF0000FF, 0xFF333388,
		0xFF0000AA, 0xFF111166, 0xFF000022, 0,  0,  0,  0,  0,  0,  0xFF666666,
		0xFF99EEBB, 0xFF66CC77, 0xFF226611, 0xFF44AA33, 0xFF008833, 0xFF557755,
		0xFF225500, 0xFF113322, 0xFF002211, 0,  0,  0,  0,  0,  0,  0xFF555555,
		0xFFDDFFBB, 0xFFCCFF88, 0xFF88AA55, 0xFFAADD88, 0xFF88FF00, 0xFFAABB99,
		0xFF66BB00, 0xFF559900, 0xFF336600, 0,  0,  0,  0,  0,  0,  0xFF444444,
		0xFFBBDDFF, 0xFF77CCFF, 0xFF335599, 0xFF6699FF, 0xFF1177FF, 0xFF4477AA,
		0xFF224477, 0xFF002277, 0xFF001144, 0,  0,  0,  0,  0,  0,  0xFF333333,
		0xFFAAFFFF, 0xFF55FFFF, 0xFF0088BB, 0xFF55BBCC, 0xFF00CCFF, 0xFF4499AA,
		0xFF006688, 0xFF004455, 0xFF002233, 0,  0,  0,  0,  0,  0,  0xFF222222,
		0xFFCCFFEE, 0xFFAAEEDD, 0xFF33CCAA, 0xFF55EEBB, 0xFF00FFCC, 0xFF77AAAA,
		0xFF00AA99, 0xFF008877, 0xFF004433, 0,  0,  0,  0,  0,  0,  0xFF000000,
		0xFFAAFFAA, 0xFF77FF77, 0xFF66DD44, 0xFF00FF00, 0xFF22DD22, 0xFF55BB55,
		0xFF00BB00, 0xFF008800, 0xFF224422, 0,  0,  0,  0,  0,  0,  0x00000000,
	};
}
