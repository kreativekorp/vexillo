package com.kreative.vexillo.font;

import java.util.Arrays;

public class CodeSequence implements Comparable<CodeSequence> {
	private final int[] codePoints;
	
	public CodeSequence(int[] codePoints) {
		this.codePoints = new int[codePoints.length];
		for (int i = 0; i < codePoints.length; i++) {
			this.codePoints[i] = codePoints[i];
		}
	}
	
	public CodeSequence(String s) {
		String[] pieces = s.split("[^0-9A-Fa-f]+");
		int[] pieceValues = new int[pieces.length];
		int pieceCount = 0;
		for (String piece : pieces) {
			if (piece.length() > 0) {
				try {
					int value = Integer.parseInt(piece, 16);
					pieceValues[pieceCount] = value;
					pieceCount++;
				} catch (NumberFormatException e) {
					continue;
				}
			}
		}
		this.codePoints = new int[pieceCount];
		for (int i = 0; i < pieceCount; i++) {
			this.codePoints[i] = pieceValues[i];
		}
	}
	
	public int codePointAt(int i) {
		return this.codePoints[i];
	}
	
	public int length() {
		return this.codePoints.length;
	}
	
	public int[] toArray() {
		int[] codePoints = new int[this.codePoints.length];
		for (int i = 0; i < this.codePoints.length; i++) {
			codePoints[i] = this.codePoints[i];
		}
		return codePoints;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < codePoints.length; i++) {
			if (first) first = false;
			else sb.append('+');
			String h = Integer.toHexString(codePoints[i]).toUpperCase();
			for (int j = h.length(); j < 4; j++) sb.append('0');
			sb.append(h);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof CodeSequence) {
			CodeSequence that = (CodeSequence)o;
			return Arrays.equals(this.codePoints, that.codePoints);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.codePoints);
	}
	
	@Override
	public int compareTo(CodeSequence that) {
		for (int i = 0; i < this.codePoints.length && i < that.codePoints.length; i++) {
			if (this.codePoints[i] != that.codePoints[i]) {
				return this.codePoints[i] - that.codePoints[i];
			}
		}
		return this.codePoints.length - that.codePoints.length;
	}
}