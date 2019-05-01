package com.kreative.vexillo.font;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import com.kreative.vexillo.core.Base64OutputStream;

public class ImageSVGExporter {
	public static void exportToFile(
		BufferedImage image, String format, String mimeType,
		int x, int y, int w, int h, File file
	) throws IOException {
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter wr = new OutputStreamWriter(os, "UTF-8");
		wr.append(exportToString(image, format, mimeType, x, y, w, h));
		wr.flush();
		wr.close();
	}
	
	public static String exportToString(
		BufferedImage image, String format, String mimeType,
		int x, int y, int w, int h
	) throws IOException {
		StringBuffer svg = new StringBuffer();
		svg.append("<svg id=\"glyph{{{0}}}\"");
		svg.append(" xmlns=\"http://www.w3.org/2000/svg\"");
		svg.append(" xmlns:xlink=\"http://www.w3.org/1999/xlink\">");
		String url = encodeImageDataURL(image, format, mimeType);
		svg.append("<image x=\"" + x + "\" y=\"" + y + "\"");
		svg.append(" width=\"" + w + "\" height=\"" + h + "\"");
		svg.append(" xlink:href=\"" + url + "\"/>");
		svg.append("</svg>");
		return svg.toString();
	}
	
	public static String encodeImageDataURL(
		BufferedImage image, String format, String mimeType
	) throws IOException {
		StringBuffer data = new StringBuffer("data:");
		data.append(mimeType); data.append(";base64,");
		OutputStream out = new Base64OutputStream(data);
		ImageIO.write(image, format, out);
		out.flush(); out.close();
		return data.toString();
	}
}