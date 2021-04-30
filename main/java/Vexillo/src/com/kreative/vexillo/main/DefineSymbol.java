package com.kreative.vexillo.main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefineSymbol {
	public static void main(String[] args) {
		main(Vexillo.arg0(DefineSymbol.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		String symbolId = "text";
		boolean parseOpts = true;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parseOpts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parseOpts = false;
				} else if (arg.equals("-n") && argi < args.length) {
					symbolId = args[argi++].trim();
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				String fn = file.getName();
				try {
					if (fn.toLowerCase().endsWith(".svg")) {
						writeSymbol(symbolId, file);
						continue;
					}
					System.err.println("Unknown file format: " + fn);
				} catch (IOException e) {
					System.err.println("Error reading file: " + fn + ": " + e);
				}
			}
		}
	}
	
	private static final Pattern VIEWBOX = Pattern.compile("\\s+viewBox\\s*=\\s*([\"\'])\\s*(-?[0-9.]+)\\s*(-?[0-9.]+)\\s*(-?[0-9.]+)\\s*(-?[0-9.]+)\\s*\\1", Pattern.CASE_INSENSITIVE);
	private static final Pattern TRANSFORM = Pattern.compile("\\s+transform\\s*=\\s*(\"[^\"]*\"|\'[^\']*\')", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATH = Pattern.compile("<\\s*path\\b[^>]*?\\bd\\s*=\\s*(\"[^\"]*\"|\'[^\']*\')", Pattern.CASE_INSENSITIVE);
	
	private static void writeSymbol(String symbolId, File file) throws IOException {
		StringBuffer sb = new StringBuffer();
		Scanner scan = new Scanner(file);
		while (scan.hasNextLine()) {
			sb.append(scan.nextLine());
			sb.append("\n");
		}
		scan.close();
		String s = sb.toString();
		
		Matcher m = VIEWBOX.matcher(s);
		if (!m.find()) throw new IOException("must contain a single viewBox attribute");
		double vbx = Double.parseDouble(m.group(2));
		double vby = Double.parseDouble(m.group(3));
		double vbw = Double.parseDouble(m.group(4));
		double vbh = Double.parseDouble(m.group(5));
		if (m.find()) throw new IOException("must contain a single viewBox attribute");
		
		m = TRANSFORM.matcher(s);
		if (m.find()) throw new IOException("must not contain a transform attribute");
		
		m = PATH.matcher(s);
		if (!m.find()) throw new IOException("must contain a single path object");
		String path = m.group(1).replaceAll("^[\"\']|[\"\']$", "");
		if (m.find()) throw new IOException("must contain a single path object");
		
		double scale = 1/Math.min(vbw,vbh), xt = -vbx-vbw/2, yt = -vby-vbh/2;
		String p = RewriteSVGPath.rewriteSVGPath(path, scale, scale, xt, yt);
		String[] lines = RewriteSVGPath.SplitType.PRETTY.splitRewrittenSVGPath(p, 50).split("\n");
		
		String pfx1 = "\t\t<symdef id=\"" + xmlEscape(symbolId) + "\" d=\"";
		String pfxn = "\t\t" + spaces(pfx1.length() - 2);
		String sfxn = "\"/>";
		int i = 0, n = lines.length;
		while (i < n) {
			System.out.print((i == 0) ? pfx1 : pfxn);
			System.out.print(lines[i]); i++;
			System.out.println((i == n) ? sfxn : "");
		}
	}
	
	private static String xmlEscape(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&#34;");
		s = s.replace("\'", "&#39;");
		return s;
	}
	
	private static String spaces(int n) {
		String s = " ";
		while (s.length() < n) s += s;
		return s.substring(0, n);
	}
}
