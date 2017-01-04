package com.kreative.vexillo.main;

import java.util.HashMap;
import java.util.Map;
import com.kreative.vexillo.core.Dimension;
import com.kreative.vexillo.core.DimensionParser;

public class VexCalc {
	public static void main(String[] args) {
		main(Vexillo.arg0(VexCalc.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		if (argi >= args.length) { printHelp(arg0); return; }
		for (int i = argi; i < args.length; i++) {
			if (args[i].equals("--help")) {
				printHelp(arg0);
				return;
			}
		}
		Map<String, Dimension> ns = createNamespace();
		String[] lines = join(args, argi, " ").split(";");
		for (String line : lines) {
			try {
				if (line.contains(":=")) {
					String[] parts = line.split(":=", 2);
					String n = parts[0].trim();
					Dimension d = new DimensionParser(parts[1]).parse();
					ns.put(n, d);
				} else {
					Dimension d = new DimensionParser(line).parse();
					double v = d.value(ns);
					if (v == (int)v) System.out.println((int)v);
					else System.out.println(v);
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("VexCalc - Vexillo calculator.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " <expression>");
		System.out.println();
	}
	
	private static Map<String, Dimension> createNamespace() {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		ns.put("pi", new Dimension.Constant(Math.PI));
		ns.put("e", new Dimension.Constant(Math.E));
		return ns;
	}
	
	private static String join(String[] a, int o, String d) {
		StringBuffer s = new StringBuffer();
		for (int i = o; i < a.length; i++) {
			if (i > o) s.append(d);
			s.append(a[i]);
		}
		return s.toString();
	}
}