package com.kreative.vexillo.core;

import java.io.IOException;
import java.io.InputStream;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class ASCII85InputStream extends InputStream {
	private final CharacterIterator ci;
	private final InputStream in;
	
	public ASCII85InputStream(String s) {
		this.ci = new StringCharacterIterator(s);
		this.in = null;
	}
	
	public ASCII85InputStream(CharacterIterator ci) {
		this.ci = ci;
		this.in = null;
	}
	
	public ASCII85InputStream(InputStream in) {
		this.ci = null;
		this.in = in;
	}
	
	private long word = 0;
	private int count = 0;
	private int rbyte = 0;
	private int rcount = 0;
	private boolean eof = false;
	
	@Override
	public int read() throws IOException {
		for (;;) {
			if (count > 0) {
				int b = (int)((word >> 24) & 0xFF);
				word <<= 8;
				count--;
				return b;
			}
			if (rcount > 0) {
				rcount--;
				return rbyte;
			}
			if (eof) return -1;
			readWord();
		}
	}
	
	private void readWord() throws IOException {
		for (;;) {
			int c = -1;
			if (ci != null) { c = ci.current(); ci.next(); }
			if (in != null) { c = in.read(); }
			if (c < 0 || c == '~' || c == CharacterIterator.DONE) {
				padWord();
				eof = true;
				return;
			} else if (c >= 'x' && c <= 'z') {
				padWord();
				switch (c) {
					case 'z': rbyte = 0x00; break;
					case 'y': rbyte = 0x20; break;
					case 'x': rbyte = 0xFF; break;
				}
				rcount = 4;
				return;
			} else if (c > ' ' && c < '~') {
				switch (c) {
					case 'w': c = '\"'; break;
					case 'v': c = '\''; break;
					case '{': c = '<'; break;
					case '}': c = '>'; break;
					case '|': c = '&'; break;
				}
				word *= 85;
				word += (c - '!');
				count++;
				if (count > 4) {
					count = 4;
					return;
				}
			}
		}
	}
	
	private void padWord() {
		if (count > 0) {
			for (int i = count; i <= 4; i++) {
				word *= 85;
				word += 84;
			}
			count--;
		}
	}
	
	@Override
	public void close() throws IOException {
		if (in != null) in.close();
	}
}