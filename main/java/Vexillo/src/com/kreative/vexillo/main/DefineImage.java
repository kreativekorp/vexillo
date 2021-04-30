package com.kreative.vexillo.main;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import com.kreative.vexillo.core.ASCII85OutputStream;

public class DefineImage {
	public static void main(String[] args) {
		main(Vexillo.arg0(DefineImage.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		String imageId = "seal";
		boolean anyWritten = false;
		boolean parseOpts = true;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("-n") && argi < args.length) {
					if (anyWritten) System.out.println("\t\t</imgdef>");
					imageId = args[argi++].trim();
					anyWritten = false;
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				String fn = file.getName();
				try {
					int o = fn.lastIndexOf('.');
					if (o > 0) {
						String ext = fn.substring(o).toLowerCase();
						if (ext.equals(".svg")) {
							anyWritten = writeRaw(imageId, anyWritten, file, "image/svg+xml");
							continue;
						}
						if (ext.equals(".png")) {
							anyWritten = writeASCII85(imageId, anyWritten, file, "image/png");
							continue;
						}
						if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".jpe")) {
							anyWritten = writeASCII85(imageId, anyWritten, file, "image/jpeg");
							continue;
						}
						if (ext.equals(".gif")) {
							anyWritten = writeASCII85(imageId, anyWritten, file, "image/gif");
							continue;
						}
					}
					System.err.println("Unknown file format: " + fn);
				} catch (IOException e) {
					System.err.println("Error reading file: " + fn + ": " + e);
				}
			}
		}
		if (anyWritten) System.out.println("\t\t</imgdef>");
	}
	
	private static boolean writeRaw(String imageId, boolean anyWritten, File file, String type) throws IOException {
		boolean thisWritten = false;
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			if (line.trim().length() == 0) continue;
			if (!anyWritten) System.out.println("\t\t<imgdef id=\"" + xmlEscape(imageId) + "\">");
			if (!thisWritten) {
				System.out.println("\t\t\t<imgsrc type=\"" + xmlEscape(type) + "\" enc=\"raw\">");
				System.out.println("\t\t\t\t<![CDATA[");
			}
			System.out.println("\t\t\t\t\t" + line);
			thisWritten = true;
			anyWritten = true;
		}
		scan.close();
		if (thisWritten) {
			System.out.println("\t\t\t\t]]>");
			System.out.println("\t\t\t</imgsrc>");
		}
		return anyWritten;
	}
	
	private static boolean writeASCII85(String imageId, boolean anyWritten, File file, String type) throws IOException {
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		ASCII85OutputStream as = new ASCII85OutputStream(bs, false, false, false, true);
		FileInputStream in = new FileInputStream(file);
		for (int b = in.read(); b >= 0; b = in.read()) as.write(b);
		in.close(); as.close(); bs.close();
		String s = bs.toString("US-ASCII");
		s = s.replace("]]>", "] ] >");
		
		boolean thisWritten = false;
		int i = 0, n = s.length();
		while (i < n) {
			String line = s.substring(i, Math.min(i + 4096, n));
			if (!anyWritten) System.out.println("\t\t<imgdef id=\"" + xmlEscape(imageId) + "\">");
			if (!thisWritten) {
				System.out.println("\t\t\t<imgsrc type=\"" + xmlEscape(type) + "\" enc=\"ascii85\">");
				System.out.println("\t\t\t\t<![CDATA[");
			}
			System.out.println("\t\t\t\t\t" + line);
			thisWritten = true;
			anyWritten = true;
			i += line.length();
		}
		if (thisWritten) {
			System.out.println("\t\t\t\t]]>");
			System.out.println("\t\t\t</imgsrc>");
		}
		return anyWritten;
	}
	
	private static String xmlEscape(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&#34;");
		s = s.replace("\'", "&#39;");
		return s;
	}
}
