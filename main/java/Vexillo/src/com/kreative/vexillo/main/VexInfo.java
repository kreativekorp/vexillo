package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.kreative.vexillo.core.Color;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.core.PolygonExporter;

public class VexInfo {
	public static void main(String[] args) {
		main(Vexillo.arg0(VexInfo.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		try { System.setProperty("apple.awt.UIElement", "true"); } catch (Exception e) {}
		if (argi >= args.length) { printHelp(arg0); return; }
		String fieldString = args[argi++];
		if (argi >= args.length) { printHelp(arg0); return; }
		List<Field> fields = new LinkedList<Field>();
		CharacterIterator it = new StringCharacterIterator(fieldString);
		for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
			try { fields.add(Field.valueOf(Character.toString(Character.toUpperCase(ch)))); }
			catch (Exception e) {}
		}
		PrintWriter out;
		try { out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true); }
		catch (IOException e) { out = new PrintWriter(new OutputStreamWriter(System.out), true); }
		while (argi < args.length) {
			String arg = args[argi++];
			File inputFile = new File(arg);
			Flag flag = readFile(inputFile);
			if (flag != null) renderFlag(inputFile, flag, fields, out);
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("VexInfo - Print information about Vexillo files.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " <fields> <files>");
		System.out.println();
		System.out.println("<Fields> is a string of letters:");
		for (Field field : Field.values()) {
			System.out.println("  " + field.name().toLowerCase() + "   " + field.description);
		}
		System.out.println();
	}
	
	private static Flag readFile(File file) {
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
	
	private static void renderFlag(File file, Flag flag, List<Field> fields, PrintWriter out) {
		for (int i = 0, n = fields.size(); i < n; i++) {
			String value = fields.get(i).getValue(file, flag);
			if (value != null) out.print(value);
			if (i < n - 1) out.print("\t");
			else out.println();
		}
	}
	
	private static enum Field {
		A("aspect ratio (width / height)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return Double.toString(flag.getWidthFromHeight2D(1));
			}
		},
		B("aspect ratio (height / width)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return Double.toString(flag.getHeightFromWidth2D(1));
			}
		},
		C("colors") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				StringBuffer sb = new StringBuffer();
				boolean first = true;
				for (Map.Entry<String,Color> e : flag.colors().entrySet()) {
					if (first) first = false;
					else sb.append("; ");
					sb.append(e.getKey());
					sb.append(": ");
					sb.append(e.getValue());
				}
				return sb.toString();
			}
		},
		E("file extension") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				String fn = flagFile.getName();
				int i = fn.lastIndexOf('.');
				if (i > 0) fn = fn.substring(i + 1);
				return fn;
			}
		},
		F("file name with extension") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flagFile.getName();
			}
		},
		G("file name without extension") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				String fn = flagFile.getName();
				int i = fn.lastIndexOf('.');
				if (i > 0) fn = fn.substring(0, i);
				return fn;
			}
		},
		H("file path") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flagFile.getAbsolutePath();
			}
		},
		I("flag id") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flag.getId();
			}
		},
		J("flag id (or file name without extension)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				String id = flag.getId();
				if (id != null && id.length() > 0) return id;
				String fn = flagFile.getName();
				int i = fn.lastIndexOf('.');
				if (i > 0) fn = fn.substring(0, i);
				return fn;
			}
		},
		M("flag name (or file name without extension)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				String name = flag.getName();
				if (name != null && name.length() > 0) return name;
				String fn = flagFile.getName();
				int i = fn.lastIndexOf('.');
				if (i > 0) fn = fn.substring(0, i);
				return fn;
			}
		},
		N("flag name") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flag.getName();
			}
		},
		P("properties") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flag.getProperties().toString();
			}
		},
		Q("properties (private use codepoints)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flag.getProperties().getCodePointString();
			}
		},
		R("aspect ratio (string)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				return flag.getProportionString();
			}
		},
		V("polygons (VGA 640x480)") {
			@Override
			public String getValue(File flagFile, Flag flag) {
				PolygonExporter polyexpo = new PolygonExporter(flagFile, flagFile.getParentFile(), flag);
				return PolygonExporter.toCIntArrayString(polyexpo.getPolygons(640, 480, null, 4));
			}
		};
		public final String description;
		private Field(String description) { this.description = description; }
		public abstract String getValue(File flagFile, Flag flag);
	}
}