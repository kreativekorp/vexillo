package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.font.Encoding;
import com.kreative.vexillo.font.FlagFontFamily;
import com.kreative.vexillo.font.SFDExporter;
import com.kreative.vexillo.style.Stylizer;

public class VexMoji {
	public static void main(String[] args) {
		main(Vexillo.arg0(VexMoji.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		try { System.setProperty("apple.awt.UIElement", "true"); } catch (Exception e) {}
		if (argi >= args.length) { printHelp(arg0); return; }
		Options o = new Options();
		while (argi < args.length) {
			String arg = args[argi++];
			if (o.parsingOptions && arg.startsWith("-")) {
				if (arg.equals("--")) {
					o.parsingOptions = false;
				} else if (arg.equals("--cbdt")) {
					o.checkFormat(false);
					o.cbdt = true;
				} else if (arg.equals("--nocbdt")) {
					o.checkFormat(true);
					o.cbdt = false;
				} else if (arg.equals("--sbix")) {
					o.checkFormat(false);
					o.sbix = true;
				} else if (arg.equals("--nosbix")) {
					o.checkFormat(true);
					o.sbix = false;
				} else if (arg.equals("--svg")) {
					o.checkFormat(false);
					o.svg = true;
				} else if (arg.equals("--nosvg")) {
					o.checkFormat(true);
					o.svg = false;
				} else if (arg.equals("-o") && argi < args.length) {
					o.outputFile = new File(args[argi++]);
				} else if (arg.equals("-e") && argi < args.length) {
					o.encoding = readEncoding(new File(args[argi++]));
					if (o.encoding == null) return;
				} else if (arg.equals("-f") && argi < args.length) {
					o.flagFiles.add(new File(args[argi++]));
				} else if (arg.equals("-n") && argi < args.length) {
					o.name = args[argi++];
				} else if (arg.equals("-c") && argi < args.length) {
					o.copyright = args[argi++];
				} else if (arg.equals("-v") && argi < args.length) {
					o.vendorId = args[argi++];
				} else if (arg.equals("-ma") && argi < args.length) {
					try { o.emAscent = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.emAscent = 0; }
				} else if (arg.equals("-md") && argi < args.length) {
					try { o.emDescent = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.emDescent = 0; }
				} else if (arg.equals("-la") && argi < args.length) {
					try { o.lineAscent = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.lineAscent = 0; }
				} else if (arg.equals("-ld") && argi < args.length) {
					try { o.lineDescent = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.lineDescent = 0; }
				} else if (arg.equals("-sw") && argi < args.length) {
					try { o.spaceWidth = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.spaceWidth = 0; }
				} else if (arg.equals("-lb") && argi < args.length) {
					try { o.leftBearing = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.leftBearing = 0; }
				} else if (arg.equals("-rb") && argi < args.length) {
					try { o.rightBearing = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.rightBearing = 0; }
				} else if (arg.equals("-gb") && argi < args.length) {
					try { o.glyphBottom = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.glyphBottom = 0; }
				} else if (arg.equals("-gh") && argi < args.length) {
					try { o.glyphHeight = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.glyphHeight = 0; }
				} else if (arg.equals("-gw") && argi < args.length) {
					try { o.glyphWidth = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.glyphWidth = 0; }
				} else if (arg.equals("-bh") && argi < args.length) {
					try { o.bitmapHeight = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.bitmapHeight = 0; }
				} else if (arg.equals("-bw") && argi < args.length) {
					try { o.bitmapWidth = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.bitmapWidth = 0; }
				} else if (arg.equals("-bg") && argi < args.length) {
					try { o.bitmapGlaze = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.bitmapGlaze = 0; }
				} else if (arg.equals("-by") && argi < args.length) {
					o.bitmapStyle = loadStylizer(args[argi++]);
				} else if (arg.equals("--help")) {
					printHelp(arg0);
					o.printedHelp = true;
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else if (o.outputFile == null) {
				o.outputFile = new File(arg);
			} else if (o.encoding == null) {
				o.encoding = readEncoding(new File(arg));
				if (o.encoding == null) return;
			} else {
				o.flagFiles.add(new File(arg));
			}
		}
		if (!o.checkFormat(true) || o.outputFile == null || o.encoding == null || o.flagFiles.isEmpty()) {
			if (!o.printedHelp) printHelp(arg0);
		} else {
			process(o);
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("VexMoji - Export Vexillo files to emoji fonts.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] <output-file> <encoding-file> <flag-files>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  --[no]cbdt      [Do not] generate a cbdt directory.");
		System.out.println("  --[no]sbix      [Do not] generate an sbix directory.");
		System.out.println("  --[no]svg       [Do not] generate an svg directory.");
		System.out.println("  -o <path>       Set output file.");
		System.out.println("  -e <path>       Set encoding file.");
		System.out.println("  -f <path>       Add flag file.");
		System.out.println("  -n <string>     Set font family name.");
		System.out.println("  -c <string>     Set font copyright string.");
		System.out.println("  -v <fcc>        Set font vendor ID.");
		System.out.println("  -ma <units>     Set font scaling ascent.");
		System.out.println("  -md <units>     Set font scaling descent.");
		System.out.println("  -la <units>     Set line spacing ascent.");
		System.out.println("  -ld <units>     Set line spacing descent.");
		System.out.println("  -sw <units>     Set width of spaces.");
		System.out.println("  -lb <units>     Set left bearing of glyphs.");
		System.out.println("  -rb <units>     Set right bearing of glyphs.");
		System.out.println("  -gb <units>     Set glyph baseline.");
		System.out.println("  -gh <units>     Set glyph height.");
		System.out.println("  -gw <units>     Set glyph width. Set to 0 to calculate from flag geometry.");
		System.out.println("  -bh <pixels>    Set image height.");
		System.out.println("  -bw <pixels>    Set image width. Set to 0 to calculate from flag geometry.");
		System.out.println("  -bg <pixels>    Add glazing like on FamFamFam flag icons. Set to 0 to disable.");
		System.out.println("  -by <classname> Use a Stylizer class to generate stylized flag images.");
		System.out.println("  --              Treat remaining arguments as file names.");
		System.out.println();
		System.out.println("VexMoji outputs an sfd file to be compiled into a ttf file by FontForge,");
		System.out.println("and image directories to be injected into the ttf file by Bits\'n\'Picas.");
		System.out.println();
	}
	
	private static Stylizer loadStylizer(String name) {
		if (name.length() > 0) {
			if (!name.contains(".")) {
				if (!name.contains("Stylizer")) {
					String fi = name.substring(0, 1).toUpperCase();
					name = fi + name.substring(1) + "Stylizer";
				}
				name = "com.kreative.vexillo.style." + name;
			}
			try {
				return Class.forName(name).asSubclass(Stylizer.class).newInstance();
			} catch (Exception e) {
				System.err.println("Unknown stylizer class: " + name);
			}
		}
		return null;
	}
	
	private static void process(Options o) {
		FlagFontFamily font = new FlagFontFamily(o.encoding);
		font.name = o.name;
		font.copyright = o.copyright;
		font.vendorId = o.vendorId;
		font.emAscent = o.emAscent;
		font.emDescent = o.emDescent;
		font.lineAscent = o.lineAscent;
		font.lineDescent = o.lineDescent;
		font.spaceWidth = o.spaceWidth;
		font.leftBearing = o.leftBearing;
		font.rightBearing = o.rightBearing;
		font.glyphBottom = o.glyphBottom;
		font.glyphHeight = o.glyphHeight;
		font.glyphWidth = o.glyphWidth;
		font.bitmapHeight = o.bitmapHeight;
		font.bitmapWidth = o.bitmapWidth;
		font.bitmapGlaze = o.bitmapGlaze;
		font.bitmapStyle = o.bitmapStyle;
		System.out.print("Generating " + o.outputFile.getName() + " ...");
		for (File flagFile : o.flagFiles) {
			Flag flag = readFlag(flagFile);
			if (flag == null) continue;
			String id = flag.getId();
			if (id == null || id.length() == 0) {
				id = flagFile.getName();
				int i = id.lastIndexOf('.');
				if (i > 0) id = id.substring(0, i);
			}
			boolean added = font.addFlag(id, flagFile, flagFile.getParentFile(), flag);
			if (!added) System.err.println("Warning: No encoding for " + flagFile.getName());
		}
		System.out.print(" ...");
		writeFont(o.outputFile, font, o);
		System.out.println(" done.");
	}
	
	private static Encoding readEncoding(File file) {
		try {
			Scanner scanner = new Scanner(file);
			Encoding encoding = new Encoding().parse(scanner);
			scanner.close();
			return encoding;
		} catch (IOException e) {
			System.err.println(
				"Error reading " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println(
				"Error compiling " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			return null;
		}
	}
	
	private static Flag readFlag(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			Flag flag = FlagParser.parse(file.getName(), in);
			in.close();
			return flag;
		} catch (IOException e) {
			System.err.println(
				"Error reading " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			return null;
		} catch (RuntimeException e) {
			System.err.println(
				"Error compiling " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			return null;
		}
	}
	
	private static void writeFont(File out, FlagFontFamily font, Options o) {
		try {
			SFDExporter exp = new SFDExporter(out, font);
			if (o.cbdt) exp.includeCbdt();
			if (o.sbix) exp.includeSbix();
			if (o.svg) exp.includeSvg();
			exp.export();
		} catch (IOException e) {
			System.err.println(
				"Error writing " + out.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
		}
	}
	
	private static class Options {
		public boolean parsingOptions = true;
		public boolean setFormat = false;
		public boolean cbdt = false;
		public boolean sbix = false;
		public boolean svg = false;
		public File outputFile = null;
		public Encoding encoding = null;
		public List<File> flagFiles = new LinkedList<File>();
		public boolean printedHelp = false;
		
		public String name = "Kreative Vexillo";
		public String copyright = (
			"Copyright 2017-" +
			new GregorianCalendar().get(Calendar.YEAR) +
			" Kreative Software"
		);
		public String vendorId = "KrKo";
		public int emAscent = 900;
		public int emDescent = 300;
		public int lineAscent = 1200;
		public int lineDescent = 400;
		
		public int spaceWidth = 400;
		public int leftBearing = 0;
		public int rightBearing = 0;
		public int glyphBottom = -100;
		public int glyphHeight = 1100;
		public int glyphWidth = 1600;
		public int bitmapHeight = 88;
		public int bitmapWidth = 128;
		public int bitmapGlaze = 8;
		public Stylizer bitmapStyle = null;
		
		public boolean checkFormat(boolean def) {
			if (setFormat) {
				return (cbdt || sbix || svg);
			} else {
				setFormat = true;
				cbdt = sbix = svg = def;
				return def;
			}
		}
	}
}