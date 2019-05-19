package com.kreative.vexillo.webapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.kreative.vexillo.core.Color;
import com.kreative.vexillo.core.Color.HasRGB;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.core.PropertySet;
import com.kreative.vexillo.font.CodeSequence;
import com.kreative.vexillo.font.Encoding;
import com.kreative.vexillo.font.EncodingNode;

public class MakeJS {
	public static void main(String[] args) throws IOException {
		try { System.setProperty("apple.awt.UIElement", "true"); } catch (Exception e) {}
		boolean parsingOptions = true;
		File outputFile = new File("vexdata.js");
		Encoding encoding = new Encoding();
		KeywordSet keywords = new KeywordSet();
		NameSet names = new NameSet();
		PatternListComparator.ForStrings sortOrder = new PatternListComparator.ForStrings();
		Map<String,Flag> flags = new HashMap<String,Flag>();
		List<String> ids = new ArrayList<String>();
		int argi = 0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (parsingOptions && arg.startsWith("-")) {
				if (arg.equals("--")) {
					parsingOptions = false;
				} else if (arg.equals("-o") && argi < args.length) {
					outputFile = new File(args[argi++]);
				} else if (arg.equals("-e") && argi < args.length) {
					Scanner scanner = new Scanner(new File(args[argi++]));
					encoding.parse(scanner);
					scanner.close();
				} else if (arg.equals("-k") && argi < args.length) {
					Scanner scanner = new Scanner(new File(args[argi++]));
					keywords.parse(scanner);
					scanner.close();
				} else if (arg.equals("-n") && argi < args.length) {
					Scanner scanner = new Scanner(new File(args[argi++]));
					names.parse(scanner);
					scanner.close();
				} else if (arg.equals("-s") && argi < args.length) {
					Scanner scanner = new Scanner(new File(args[argi++]));
					sortOrder.parse(scanner);
					scanner.close();
				} else if (arg.equals("-f") && argi < args.length) {
					File file = new File(args[argi++]);
					FileInputStream in = new FileInputStream(file);
					Flag flag = FlagParser.parse(file.getName(), in);
					in.close();
					String id = file.getName();
					int o = id.indexOf(".");
					if (o > 0) id = id.substring(0, o);
					flags.put(id, flag);
					ids.add(id);
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File file = new File(arg);
				FileInputStream in = new FileInputStream(file);
				Flag flag = FlagParser.parse(file.getName(), in);
				in.close();
				String id = file.getName();
				int o = id.indexOf(".");
				if (o > 0) id = id.substring(0, o);
				flags.put(id, flag);
				ids.add(id);
			}
		}
		
		Collections.sort(ids, sortOrder);
		PrintWriter out = open(outputFile);
		out.println("(function(V){");
		
		out.println("V.encoding = [");
		for (EncodingNode node : encoding.getNodes()) {
			out.println("[" + quote(node.toIdArray()) + "," + quoteless(node.toCodeSequenceArray()) + "],");
		}
		out.println("];");
		
		out.println("V.keywords = [");
		for (KeywordNode node : keywords.getNodes()) {
			out.println("[" + quote(node.toIdArray()) + "," + quote(node.toKeywordArray()) + "],");
		}
		out.println("];");
		
		out.println("V.namelist = [");
		for (NameNode node : names.getNodes()) {
			out.println("[" + quote(node.toIdArray()) + "," + quote(node.getName()) + "],");
		}
		out.println("];");
		
		out.println("V.flaglist = [");
		for (String id : ids) {
			Flag flag = flags.get(id);
			out.print("{");
			out.print("id:" + quote(id) + ",");
			String name = flag.getName();
			if (name != null) {
				out.print("name:" + quote(name) + ",");
			}
			out.print("ar:" + flag.getWidthFromHeight2D(1) + ",");
			out.print("ars:" + quote(flag.getProportionString()) + ",");
			PropertySet props = flag.getProperties();
			if (props != null && !props.isEmpty()) {
				out.print("props:" + quote(props.toString().split(" ")) + ",");
				out.print("pcps:" + quote(props.getCodePointString()) + ",");
			}
			out.print("colors:" + quoteColors(flag.colors()));
			out.println("},");
		}
		out.println("];");
		
		out.print("})(window.Vexillo || (window.Vexillo = {}));");
		out.flush();
		out.close();
	}
	
	private static PrintWriter open(File file) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
		return new PrintWriter(writer, true);
	}
	
	private static String quote(String s) {
		s = s.replaceAll("\\\\", "\\\\\\\\");
		s = s.replaceAll("\"", "\\\\\"");
		return "\"" + s + "\"";
	}
	
	private static String quote(String[] arr) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append("[");
		for (String s : arr) {
			if (first) first = false;
			else sb.append(",");
			sb.append(quote(s));
		}
		sb.append("]");
		return sb.toString();
	}
	
	private static String quoteless(int[] arr) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append("[");
		for (int i : arr) {
			if (first) first = false;
			else sb.append(",");
			sb.append(i);
		}
		sb.append("]");
		return sb.toString();
	}
	
	private static String quoteless(CodeSequence[] arr) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append("[");
		for (CodeSequence cs : arr) {
			if (first) first = false;
			else sb.append(",");
			sb.append(quoteless(cs.toArray()));
		}
		sb.append("]");
		return sb.toString();
	}
	
	private static String quoteColors(Map<String,Color> m) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		sb.append("[");
		for (Map.Entry<String,Color> e : m.entrySet()) {
			if (first) first = false;
			else sb.append(",");
			Color c = e.getValue();
			String css = "null";
			String hsvo = "[2,2,2]";
			if (c instanceof HasRGB) {
				int cssi = ((HasRGB)c).getRGB();
				int a = (cssi >> 24) & 0xFF;
				int r = (cssi >> 16) & 0xFF;
				int g = (cssi >>  8) & 0xFF;
				int b = (cssi >>  0) & 0xFF;
				if (a == 255) {
					css = Integer.toHexString(cssi);
					css = quote("#" + css.substring(2));
				} else {
					css = r + "," + g + "," + b + "," + a/255f;
					css = quote("rgba(" + css + ")");
				}
				float[] hsv = java.awt.Color.RGBtoHSB(r, g, b, new float[3]);
				hsvo = "[" + hsv[0] + "," + hsv[1] + "," + hsv[2] + "]";
			}
			sb.append("[");
			sb.append(quote(e.getKey()));
			sb.append(",");
			sb.append(css);
			sb.append(",");
			sb.append(quote(c.toString().split(", ")));
			sb.append(",");
			sb.append(hsvo);
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}
}