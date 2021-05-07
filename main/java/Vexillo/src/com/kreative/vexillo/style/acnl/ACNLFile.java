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
		return getUTF16(0x2C, 0x40);
	}
	
	public void setCreatorName(String name) {
		setUTF16(0x2C, 0x40, name);
	}
	
	public int getTownID() {
		return getInt16(0x40);
	}
	
	public void setTownID(int id) {
		setInt16(0x40, id);
	}
	
	public String getTownName() {
		return getUTF16(0x42, 0x56);
	}
	
	public void setTownName(String name) {
		setUTF16(0x42, 0x56, name);
	}
	
	public int getUnknownA() {
		return getInt16(0x56);
	}
	
	public void setUnknownA(int a) {
		setInt16(0x56, a);
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
		0xFFFFEFFF, 0xFFFF9AAD, 0xFFEF559C, 0xFFFF65AD, 0xFFFF0063, 0xFFBD4573,
		0xFFCE0052, 0xFF9C0031, 0xFF522031, 0,  0,  0,  0,  0,  0,  0xFFFFFFFF,
		0xFFFFBACE, 0xFFFF7573, 0xFFDE3010, 0xFFFF5542, 0xFFFF0000, 0xFFCE6563,
		0xFFBD4542, 0xFFBD0000, 0xFF8C2021, 0,  0,  0,  0,  0,  0,  0xFFECECEC,
		0xFFDECFBD, 0xFFFFCF63, 0xFFDE6521, 0xFFFFAA21, 0xFFFF6500, 0xFFBD8A52,
		0xFFDE4500, 0xFFBD4500, 0xFF633010, 0,  0,  0,  0,  0,  0,  0xFFDADADA,
		0xFFFFEFDE, 0xFFFFDFCE, 0xFFFFCFAD, 0xFFFFBA8C, 0xFFFFAA8C, 0xFFDE8A63,
		0xFFBD6542, 0xFF9C5531, 0xFF8C4521, 0,  0,  0,  0,  0,  0,  0xFFC8C8C8,
		0xFFFFCFFF, 0xFFEF8AFF, 0xFFCE65DE, 0xFFBD8ACE, 0xFFCE00FF, 0xFF9C659C,
		0xFF8C00AD, 0xFF520073, 0xFF310042, 0,  0,  0,  0,  0,  0,  0xFFB6B6B6,
		0xFFFFBAFF, 0xFFFF9AFF, 0xFFDE20BD, 0xFFFF55EF, 0xFFFF00CE, 0xFF8C5573,
		0xFFBD009C, 0xFF8C0063, 0xFF520042, 0,  0,  0,  0,  0,  0,  0xFFA3A3A3,
		0xFFDEBA9C, 0xFFCEAA73, 0xFF734531, 0xFFAD7542, 0xFF9C3000, 0xFF733021,
		0xFF522000, 0xFF311000, 0xFF211000, 0,  0,  0,  0,  0,  0,  0xFF919191,
		0xFFFFFFCE, 0xFFFFFF73, 0xFFDEDF21, 0xFFFFFF00, 0xFFFFDF00, 0xFFCEAA00,
		0xFF9C9A00, 0xFF8C7500, 0xFF525500, 0,  0,  0,  0,  0,  0,  0xFF7F7F7F,
		0xFFDEBAFF, 0xFFBD9AEF, 0xFF6330CE, 0xFF9C55FF, 0xFF6300FF, 0xFF52458C,
		0xFF42009C, 0xFF210063, 0xFF211031, 0,  0,  0,  0,  0,  0,  0xFF6D6D6D,
		0xFFBDBAFF, 0xFF8C9AFF, 0xFF3130AD, 0xFF3155EF, 0xFF0000FF, 0xFF31308C,
		0xFF0000AD, 0xFF101063, 0xFF000021, 0,  0,  0,  0,  0,  0,  0xFF5B5B5B,
		0xFF9CEFBD, 0xFF63CF73, 0xFF216510, 0xFF42AA31, 0xFF008A31, 0xFF527552,
		0xFF215500, 0xFF103021, 0xFF002010, 0,  0,  0,  0,  0,  0,  0xFF484848,
		0xFFDEFFBD, 0xFFCEFF8C, 0xFF8CAA52, 0xFFADDF8C, 0xFF8CFF00, 0xFFADBA9C,
		0xFF63BA00, 0xFF529A00, 0xFF316500, 0,  0,  0,  0,  0,  0,  0xFF363636,
		0xFFBDDFFF, 0xFF73CFFF, 0xFF31559C, 0xFF639AFF, 0xFF1075FF, 0xFF4275AD,
		0xFF214573, 0xFF002073, 0xFF001042, 0,  0,  0,  0,  0,  0,  0xFF242424,
		0xFFADFFFF, 0xFF52FFFF, 0xFF008ABD, 0xFF52BACE, 0xFF00CFFF, 0xFF429AAD,
		0xFF00658C, 0xFF004552, 0xFF002031, 0,  0,  0,  0,  0,  0,  0xFF121212,
		0xFFCEFFEF, 0xFFADEFDE, 0xFF31CFAD, 0xFF52EFBD, 0xFF00FFCE, 0xFF73AAAD,
		0xFF00AA9C, 0xFF008A73, 0xFF004531, 0,  0,  0,  0,  0,  0,  0xFF000000,
		0xFFADFFAD, 0xFF73FF73, 0xFF63DF42, 0xFF00FF00, 0xFF21DF21, 0xFF52BA52,
		0xFF00BA00, 0xFF008A00, 0xFF214521, 0,  0,  0,  0,  0,  0,  0x00000000,
	};
}
