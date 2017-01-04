package com.kreative.vexillo.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageSource {
	public static enum Encoding { RAW, BASE64, ASCII85 }
	
	public final String type;
	public final Encoding encoding;
	public final String source;
	public final String data;
	
	public ImageSource(String type, Encoding encoding, String source, String data) {
		this.type = type;
		this.encoding = encoding;
		this.source = source;
		this.data = data;
	}
	
	public InputStream getInputStream(File parent) throws IOException {
		if (source != null && source.length() > 0) {
			InputStream in = new FileInputStream(new File(parent, source));
			if (encoding == Encoding.BASE64) in = new Base64InputStream(in);
			if (encoding == Encoding.ASCII85) in = new ASCII85InputStream(in);
			return in;
		} else if (data != null && data.length() > 0) {
			if (encoding == Encoding.BASE64) return new Base64InputStream(data);
			if (encoding == Encoding.ASCII85) return new ASCII85InputStream(data);
			return new ByteArrayInputStream(data.getBytes("UTF-8"));
		} else {
			return null;
		}
	}
}