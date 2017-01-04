package com.kreative.vexillo.core;

import java.io.IOException;
import java.io.OutputStream;

public class ASCII85OutputStream extends OutputStream {
	private final StringBuffer sb;
	private final OutputStream out;
	private final boolean xml, x, y, z;
	
	public ASCII85OutputStream(StringBuffer sb) {
		this(sb, false, false, false, true);
	}
	
	public ASCII85OutputStream(OutputStream out) {
		this(out, false, false, false, true);
	}
	
	public ASCII85OutputStream(StringBuffer sb, boolean xml, boolean x, boolean y, boolean z) {
		this.sb = sb;
		this.out = null;
		this.xml = xml;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public ASCII85OutputStream(OutputStream out, boolean xml, boolean x, boolean y, boolean z) {
		this.sb = null;
		this.out = out;
		this.xml = xml;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	private long word = 0;
	private int count = 0;
	
	@Override
	public void write(int b) throws IOException {
		if (count < 0) {
			if (sb != null) sb.append('~');
			if (out != null) out.write('~');
			word = 0;
			count = 0;
		}
		word <<= 8;
		word |= (b & 0xFF);
		count++;
		if (count >= 4) {
			if (word == 0x00000000L && z) {
				if (sb != null) sb.append('z');
				if (out != null) out.write('z');
			} else if (word == 0x20202020L && y) {
				if (sb != null) sb.append('y');
				if (out != null) out.write('y');
			} else if (word == 0xFFFFFFFFL && x) {
				if (sb != null) sb.append('x');
				if (out != null) out.write('x');
			} else {
				writeWord();
			}
			word = 0;
			count = 0;
		}
	}
	
	@Override
	public void flush() throws IOException {
		if (count > 0) {
			for (int i = count; i < 4; i++) word <<= 8;
			writeWord();
		}
		word = -1;
		count = -1;
		if (out != null) out.flush();
	}
	
	@Override
	public void close() throws IOException {
		flush();
		if (out != null) out.close();
	}
	
	private void writeWord() throws IOException {
		for (int d = 52200625, i = 0; i <= count; d /= 85, i++) {
			char c = (char)('!' + ((word / d) % 85));
			if (xml) {
				switch (c) {
					case '&': c = '|'; break;
					case '<': c = '{'; break;
					case '>': c = '}'; break;
					case '\'': c = 'v'; break;
					case '\"': c = 'w'; break;
				}
			}
			if (sb != null) sb.append(c);
			if (out != null) out.write(c);
		}
	}
}