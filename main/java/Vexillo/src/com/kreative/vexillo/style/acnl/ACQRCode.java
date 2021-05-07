package com.kreative.vexillo.style.acnl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.BitSet;

// This class is greatly simplified for generating ACNL QR Codes ONLY.
// It cannot be used to generate QR Codes for any other purpose.
public class ACQRCode {
	private final int frameIndex;
	private final int frameCount;
	private final int parity;
	private final byte[] data;
	private final int[][] matrix;
	
	public ACQRCode(int frameIndex, int frameCount, int parity, byte[] data) {
		if (data == null) {
			throw new NullPointerException("data = null");
		} else if (data.length <= 504 || data.length > 624) {
			throw new IllegalArgumentException("data.length = " + data.length);
		} else {
			this.frameIndex = frameIndex;
			this.frameCount = frameCount;
			this.parity = parity;
			this.data = data;
			this.matrix = encode(frameIndex, frameCount, parity, data);
		}
	}
	
	public int getFrameIndex() { return frameIndex; }
	public int getFrameCount() { return frameCount; }
	public int getQRParity() { return parity; }
	public byte[] getQRData() { return data; }
	public int getSize() { return matrix.length; }
	
	public int[] getMatrixRGB(int bgcolor, int fgcolor) {
		int s = matrix.length;
		int[] rgb = new int[s * s];
		for (int i = 0, y = 0; y < s; y++) {
			for (int x = 0; x < s; x++, i++) {
				rgb[i] = (matrix[y][x] == 0) ? bgcolor : fgcolor;
			}
		}
		return rgb;
	}
	
	public int[] getMatrixRGB() {
		return getMatrixRGB(-1, 0xFF << 24);
	}
	
	public BufferedImage getMatrixImage(int qcolor, int bgcolor, int fgcolor) {
		int ms = matrix.length;
		int qs = ms + 8;
		BufferedImage image = new BufferedImage(qs, qs, BufferedImage.TYPE_INT_ARGB);
		int[] qrgb = new int[qs];
		for (int i = 0; i < qs; i++) qrgb[i] = qcolor;
		image.setRGB(0, 0, qs, qs, qrgb, 0, 0);
		int[] mrgb = getMatrixRGB(bgcolor, fgcolor);
		image.setRGB(4, 4, ms, ms, mrgb, 0, ms);
		return image;
	}
	
	public BufferedImage getMatrixImage() {
		return getMatrixImage(-1, -1, 0xFF << 24);
	}
	
	private static int[][] encode(int frameIndex, int frameCount, int parity, byte[] data) {
		// Convert from characters to bits.
		BitArray code = new BitArray();
		if (frameCount > 1) {
			// Add structured append header.
			code.addAll(0, 0, 1, 1);
			code.addByte((frameIndex << 4) | (frameCount - 1));
			code.addByte(parity);
		}
		code.addAll(0, 1, 0, 0);
		code.addByte(data.length >> 8);
		code.addByte(data.length >> 0);
		for (byte b : data) code.addByte(b);
		code.addAll(0, 0, 0, 0);
		// Convert from bits to bytes with padding.
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		while (code.hasNext()) bs.write(code.nextByte());
		boolean pro = (bs.size() <= EC_PARAMS_PRO[0]);
		int[] ecParams = pro ? EC_PARAMS_PRO : EC_PARAMS_STD;
		boolean a = true;
		for (int i = bs.size(), n = ecParams[0]; i < n; i++) {
			bs.write(a ? 236 : 17);
			a = !a;
		}
		// Add error correction and create matrix.
		data = bs.toByteArray();
		code = addErrorCorrection(data, ecParams);
		int[][] matrix = createMatrix(pro, code);
		int mask = applyBestMask(matrix, matrix.length);
		finalizeMatrix(matrix, matrix.length, mask, pro);
		return matrix;
	}
	
	private static BitArray addErrorCorrection(byte[] data, int[] ecParams) {
		byte[][] dBlocks = ecSplit(data, ecParams);
		byte[][] ecBlocks = new byte[dBlocks.length][];
		for (int i = 0, n = dBlocks.length; i < n; i++) {
			ecBlocks[i] = ecDivide(dBlocks[i], ecParams);
		}
		byte[] dData = ecInterleave(dBlocks);
		byte[] ecData = ecInterleave(ecBlocks);
		BitArray code = new BitArray();
		for (byte b : dData) code.addByte(b);
		for (byte b : ecData) code.addByte(b);
		for (int n = 3; n >= 0; n--) code.add(0);
		return code;
	}
	
	private static byte[][] ecSplit(byte[] data, int[] ecParams) {
		int numBlocks = ecParams[2] + ecParams[4];
		byte[][] blocks = new byte[numBlocks][];
		int blockPtr = 0, offset = 0;
		for (int i = ecParams[2], length = ecParams[3]; i > 0; i--) {
			byte[] block = new byte[length];
			for (int j = 0; j < length; j++) block[j] = data[offset++];
			blocks[blockPtr++] = block;
		}
		for (int i = ecParams[4], length = ecParams[5]; i > 0; i--) {
			byte[] block = new byte[length];
			for (int j = 0; j < length; j++) block[j] = data[offset++];
			blocks[blockPtr++] = block;
		}
		return blocks;
	}
	
	private static byte[] ecDivide(byte[] data, int[] ecParams) {
		int numData = data.length;
		int numError = ecParams[1];
		byte[] message = new byte[numData + numError];
		for (int i = 0; i < numData; i++) message[i] = data[i];
		for (int i = 0; i < numData; i++) {
			if (message[i] != 0) {
				int leadTerm = LOG[message[i] & 0xFF];
				for (int j = 0; j <= numError; j++) {
					int term = (EC_POLYNOMIAL[j] + leadTerm) % 255;
					message[i + j] ^= EXP[term];
				}
			}
		}
		data = new byte[numError];
		for (int i = 0; i < numError; i++) data[i] = message[numData++];
		return data;
	}
	
	private static byte[] ecInterleave(byte[][] blocks) {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		for (int offset = 0; true; offset++) {
			boolean done = true;
			for (byte[] block : blocks) {
				if (offset < block.length) {
					bs.write(block[offset]);
					done = false;
				}
			}
			if (done) break;
		}
		return bs.toByteArray();
	}
	
	private static int[][] createMatrix(boolean pro, BitArray data) {
		int size = pro ? 89 : 93;
		int[][] matrix = new int[size][size];
		// Finder patterns.
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				int m = ((i == 7 || j == 7) ? 2 :
				        ((i == 0 || j == 0 || i == 6 || j == 6) ? 3 :
				        ((i == 1 || j == 1 || i == 5 || j == 5) ? 2 : 3)));
				matrix[i][j] = m;
				matrix[size - i - 1][j] = m;
				matrix[i][size - j - 1] = m;
			}
		}
		// Alignment patterns.
		int[] alignment = pro ? ALIGNMENT_PRO : ALIGNMENT_STD;
		for (int i : alignment) {
			for (int j : alignment) {
				if (matrix[i][j] == 0) {
					for (int ii = -2; ii <= 2; ii++) {
						for (int jj = -2; jj <= 2; jj++) {
							int m = (Math.max(Math.abs(ii), Math.abs(jj)) & 1) ^ 3;
							matrix[i + ii][j + jj] = m;
						}
					}
				}
			}
		}
		// Timing patterns.
		for (int i = size - 9; i >= 8; i--) {
			matrix[i][6] = (i & 1) ^ 3;
			matrix[6][i] = (i & 1) ^ 3;
		}
		// Dark module. Such an ominous name for such an innocuous thing.
		matrix[size - 8][8] = 3;
		// Format information area.
		for (int i = 0; i <= 8; i++) {
			if (matrix[i][8] == 0) matrix[i][8] = 1;
			if (matrix[8][i] == 0) matrix[8][i] = 1;
			if (i != 0 && matrix[size - i][8] == 0) matrix[size - i][8] = 1;
			if (i != 0 && matrix[8][size - i] == 0) matrix[8][size - i] = 1;
		}
		// Version information area.
		for (int i = 9; i < 12; i++) {
			for (int j = 0; j < 6; j++) {
				matrix[size - i][j] = 1;
				matrix[j][size - i] = 1;
			}
		}
		// Data.
		int col = size - 1;
		int row = size - 1;
		int dir = -1;
		while (col > 0 && data.hasNext()) {
			if (matrix[row][col] == 0) {
				matrix[row][col] = data.next() ? 5 : 4;
			}
			if (matrix[row][col - 1] == 0) {
				matrix[row][col - 1] = data.next() ? 5 : 4;
			}
			row += dir;
			if (row < 0 || row >= size) {
				dir = -dir;
				row += dir;
				col -= 2;
				if (col == 6) col--;
			}
		}
		return matrix;
	}
	
	private static int applyBestMask(int[][] matrix, int size) {
		int bestMask = 0;
		int[][] bestMatrix = applyMask(matrix, size, bestMask);
		int bestPenalty = penalty(bestMatrix, size);
		for (int testMask = 1; testMask < 8; testMask++) {
			int[][] testMatrix = applyMask(matrix, size, testMask);
			int testPenalty = penalty(testMatrix, size);
			if (testPenalty < bestPenalty) {
				bestMask = testMask;
				bestMatrix = testMatrix;
				bestPenalty = testPenalty;
			}
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				matrix[i][j] = bestMatrix[i][j];
			}
		}
		return bestMask;
	}
	
	private static int[][] applyMask(int[][] matrix, int size, int mask) {
		int[][] newMatrix = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] >= 4 && mask(mask, i, j)) {
					newMatrix[i][j] = matrix[i][j] ^ 1;
				} else {
					newMatrix[i][j] = matrix[i][j];
				}
			}
		}
		return newMatrix;
	}
	
	private static boolean mask(int mask, int r, int c) {
		switch (mask) {
			case 0: return 0 == ( (r + c) % 2 );
			case 1: return 0 == ( (r    ) % 2 );
			case 2: return 0 == ( (    c) % 3 );
			case 3: return 0 == ( (r + c) % 3 );
			case 4: return 0 == ( (((r    ) / 2) + ((    c) / 3)) % 2 );
			case 5: return 0 == ( (((r * c) % 2) + ((r * c) % 3))     );
			case 6: return 0 == ( (((r * c) % 2) + ((r * c) % 3)) % 2 );
			case 7: return 0 == ( (((r + c) % 2) + ((r * c) % 3)) % 2 );
			default: throw new IllegalArgumentException("mask = " + mask);
		}
	}
	
	private static int penalty(int[][] matrix, int size) {
		return penalty1(matrix, size)
		     + penalty2(matrix, size)
		     + penalty3(matrix, size)
		     + penalty4(matrix, size);
	}
	
	private static int penalty1(int[][] matrix, int size) {
		int score = 0;
		for (int i = 0; i < size; i++) {
			int rowValue = 0;
			int rowCount = 0;
			int colValue = 0;
			int colCount = 0;
			for (int j = 0; j < size; j++) {
				int rv = (matrix[i][j] == 5 || matrix[i][j] == 3) ? 1 : 0;
				int cv = (matrix[j][i] == 5 || matrix[j][i] == 3) ? 1 : 0;
				if (rv == rowValue) {
					rowCount++;
				} else {
					if (rowCount >= 5) score += rowCount - 2;
					rowValue = rv;
					rowCount = 1;
				}
				if (cv == colValue) {
					colCount++;
				} else {
					if (colCount >= 5) score += colCount - 2;
					colValue = cv;
					colCount = 1;
				}
			}
			if (rowCount >= 5) score += rowCount - 2;
			if (colCount >= 5) score += colCount - 2;
		}
		return score;
	}
	
	private static int penalty2(int[][] matrix, int size) {
		int score = 0;
		for (int i = 1; i < size; i++) {
			for (int j = 1; j < size; j++) {
				int v1 = matrix[i - 1][j - 1];
				int v2 = matrix[i - 1][j - 0];
				int v3 = matrix[i - 0][j - 1];
				int v4 = matrix[i - 0][j - 0];
				v1 = (v1 == 5 || v1 == 3) ? 1 : 0;
				v2 = (v2 == 5 || v2 == 3) ? 1 : 0;
				v3 = (v3 == 5 || v3 == 3) ? 1 : 0;
				v4 = (v4 == 5 || v4 == 3) ? 1 : 0;
				if (v1 == v2 && v2 == v3 && v3 == v4) score += 3;
			}
		}
		return score;
	}
	
	private static int penalty3(int[][] matrix, int size) {
		int score = 0;
		for (int i = 0; i < size; i++) {
			int rowValue = 0;
			int colValue = 0;
			for (int j = 0; j < 11; j++) {
				int rv = (matrix[i][j] == 5 || matrix[i][j] == 3) ? 1 : 0;
				int cv = (matrix[j][i] == 5 || matrix[j][i] == 3) ? 1 : 0;
				rowValue = ((rowValue << 1) & 0x7FF) | rv;
				colValue = ((colValue << 1) & 0x7FF) | cv;
			}
			if (rowValue == 0x5D0 || rowValue == 0x5D) score += 40;
			if (colValue == 0x5D0 || colValue == 0x5D) score += 40;
			for (int j = 11; j < size; j++) {
				int rv = (matrix[i][j] == 5 || matrix[i][j] == 3) ? 1 : 0;
				int cv = (matrix[j][i] == 5 || matrix[j][i] == 3) ? 1 : 0;
				rowValue = ((rowValue << 1) & 0x7FF) | rv;
				colValue = ((colValue << 1) & 0x7FF) | cv;
				if (rowValue == 0x5D0 || rowValue == 0x5D) score += 40;
				if (colValue == 0x5D0 || colValue == 0x5D) score += 40;
			}
		}
		return score;
	}
	
	private static int penalty4(int[][] matrix, int size) {
		int dark = 0;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == 5 || matrix[i][j] == 3) {
					dark++;
				}
			}
		}
		double d = (double)(dark * 20) / (double)(size * size);
		int a = Math.abs((int)Math.floor(d) - 10);
		int b = Math.abs((int)Math.ceil(d) - 10);
		return Math.min(a, b) * 10;
	}
	
	private static void finalizeMatrix(int[][] matrix, int size, int mask, boolean pro) {
		int[] formatInfo = FORMAT_INFO[mask];
		matrix[8][0] = matrix[size - 1][8] = formatInfo[0];
		matrix[8][1] = matrix[size - 2][8] = formatInfo[1];
		matrix[8][2] = matrix[size - 3][8] = formatInfo[2];
		matrix[8][3] = matrix[size - 4][8] = formatInfo[3];
		matrix[8][4] = matrix[size - 5][8] = formatInfo[4];
		matrix[8][5] = matrix[size - 6][8] = formatInfo[5];
		matrix[8][7] = matrix[size - 7][8] = formatInfo[6];
		matrix[8][8] = matrix[8][size - 8] = formatInfo[7];
		matrix[7][8] = matrix[8][size - 7] = formatInfo[8];
		matrix[5][8] = matrix[8][size - 6] = formatInfo[9];
		matrix[4][8] = matrix[8][size - 5] = formatInfo[10];
		matrix[3][8] = matrix[8][size - 4] = formatInfo[11];
		matrix[2][8] = matrix[8][size - 3] = formatInfo[12];
		matrix[1][8] = matrix[8][size - 2] = formatInfo[13];
		matrix[0][8] = matrix[8][size - 1] = formatInfo[14];
		int[] versionInfo = pro ? VERSION_PRO : VERSION_STD;
		for (int i = 0; i < 18; i++) {
			int r = size - 9 - (i % 3);
			int c = 5 - (i / 3);
			matrix[r][c] = versionInfo[i];
			matrix[c][r] = versionInfo[i];
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				matrix[i][j] &= 1;
			}
		}
	}
	
	private static final int[] EC_PARAMS_PRO = { 563, 26, 9, 43,  4, 44 };
	private static final int[] EC_PARAMS_STD = { 627, 26, 3, 44, 11, 45 };
	
	private static final int[] EC_POLYNOMIAL = {
		0, 173, 125, 158, 2, 103, 182, 118, 17, 145, 201, 111, 28, 165,
		53, 161, 21, 245, 142, 13, 102, 48, 227, 153, 145, 218, 70
	};
	
	private static final int[] LOG = {
		  0,   0,   1,  25,   2,  50,  26, 198,   3, 223,  51, 238,  27, 104, 199,  75,
		  4, 100, 224,  14,  52, 141, 239, 129,  28, 193, 105, 248, 200,   8,  76, 113,
		  5, 138, 101,  47, 225,  36,  15,  33,  53, 147, 142, 218, 240,  18, 130,  69,
		 29, 181, 194, 125, 106,  39, 249, 185, 201, 154,   9, 120,  77, 228, 114, 166,
		  6, 191, 139,  98, 102, 221,  48, 253, 226, 152,  37, 179,  16, 145,  34, 136,
		 54, 208, 148, 206, 143, 150, 219, 189, 241, 210,  19,  92, 131,  56,  70,  64,
		 30,  66, 182, 163, 195,  72, 126, 110, 107,  58,  40,  84, 250, 133, 186,  61,
		202,  94, 155, 159,  10,  21, 121,  43,  78, 212, 229, 172, 115, 243, 167,  87,
		  7, 112, 192, 247, 140, 128,  99,  13, 103,  74, 222, 237,  49, 197, 254,  24,
		227, 165, 153, 119,  38, 184, 180, 124,  17,  68, 146, 217,  35,  32, 137,  46,
		 55,  63, 209,  91, 149, 188, 207, 205, 144, 135, 151, 178, 220, 252, 190,  97,
		242,  86, 211, 171,  20,  42,  93, 158, 132,  60,  57,  83,  71, 109,  65, 162,
		 31,  45,  67, 216, 183, 123, 164, 118, 196,  23,  73, 236, 127,  12, 111, 246,
		108, 161,  59,  82,  41, 157,  85, 170, 251,  96, 134, 177, 187, 204,  62,  90,
		203,  89,  95, 176, 156, 169, 160,  81,  11, 245,  22, 235, 122, 117,  44, 215,
		 79, 174, 213, 233, 230, 231, 173, 232, 116, 214, 244, 234, 168,  80,  88, 175,
	};
	
	private static final int[] EXP = {
		  1,   2,   4,   8,  16,  32,  64, 128,  29,  58, 116, 232, 205, 135,  19,  38,
		 76, 152,  45,  90, 180, 117, 234, 201, 143,   3,   6,  12,  24,  48,  96, 192,
		157,  39,  78, 156,  37,  74, 148,  53, 106, 212, 181, 119, 238, 193, 159,  35,
		 70, 140,   5,  10,  20,  40,  80, 160,  93, 186, 105, 210, 185, 111, 222, 161,
		 95, 190,  97, 194, 153,  47,  94, 188, 101, 202, 137,  15,  30,  60, 120, 240,
		253, 231, 211, 187, 107, 214, 177, 127, 254, 225, 223, 163,  91, 182, 113, 226,
		217, 175,  67, 134,  17,  34,  68, 136,  13,  26,  52, 104, 208, 189, 103, 206,
		129,  31,  62, 124, 248, 237, 199, 147,  59, 118, 236, 197, 151,  51, 102, 204,
		133,  23,  46,  92, 184, 109, 218, 169,  79, 158,  33,  66, 132,  21,  42,  84,
		168,  77, 154,  41,  82, 164,  85, 170,  73, 146,  57, 114, 228, 213, 183, 115,
		230, 209, 191,  99, 198, 145,  63, 126, 252, 229, 215, 179, 123, 246, 241, 255,
		227, 219, 171,  75, 150,  49,  98, 196, 149,  55, 110, 220, 165,  87, 174,  65,
		130,  25,  50, 100, 200, 141,   7,  14,  28,  56, 112, 224, 221, 167,  83, 166,
		 81, 162,  89, 178, 121, 242, 249, 239, 195, 155,  43,  86, 172,  69, 138,   9,
		 18,  36,  72, 144,  61, 122, 244, 245, 247, 243, 251, 235, 203, 139,  11,  22,
		 44,  88, 176, 125, 250, 233, 207, 131,  27,  54, 108, 216, 173,  71, 142,   1,
	};
	
	private static final int[] ALIGNMENT_PRO = { 6, 30, 56, 82 };
	private static final int[] ALIGNMENT_STD = { 6, 30, 58, 86 };
	
	private static final int[][] FORMAT_INFO = {
		{ 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0 },
		{ 1, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1 },
		{ 1, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0 },
		{ 1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 1, 1 },
		{ 1, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1 },
		{ 1, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0 },
		{ 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 0, 1, 1, 1 },
		{ 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0 },
	};
	
	private static final int[] VERSION_PRO = {0,1,0,0,1,0,1,0,1,0,0,0,0,1,0,1,1,1};
	private static final int[] VERSION_STD = {0,1,0,0,1,1,0,1,0,1,0,0,1,1,0,0,1,0};
	
	private static final class BitArray {
		private final BitSet bits = new BitSet();
		private int pointer = 0, length = 0;
		public void add(int value) { bits.set(length++, (value != 0)); }
		public void addAll(int... values) { for (int value : values) add(value); }
		public void addByte(int b) { for (int m = 0x80; m != 0; m >>= 1) add(b & m); }
		public boolean hasNext() { return pointer < length; }
		public boolean next() { return bits.get(pointer++); }
		public byte nextByte() {
			byte b = 0;
			if (next()) b |= 0x80;
			if (next()) b |= 0x40;
			if (next()) b |= 0x20;
			if (next()) b |= 0x10;
			if (next()) b |= 0x08;
			if (next()) b |= 0x04;
			if (next()) b |= 0x02;
			if (next()) b |= 0x01;
			return b;
		}
	}
}
