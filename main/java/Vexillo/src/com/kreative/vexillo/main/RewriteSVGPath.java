package com.kreative.vexillo.main;

import java.util.ArrayList;
import java.util.List;

public class RewriteSVGPath {
	public static void main(String[] args) {
		main(Vexillo.arg0(RewriteSVGPath.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		if (argi >= args.length) { printHelp(arg0); return; }
		double xs = 1.0, ys = 1.0, xt = 0.0, yt = 0.0;
		while (argi < args.length) {
			String arg = args[argi++];
			if (arg.startsWith("-")) {
				if (arg.equalsIgnoreCase("-s") && argi < args.length) {
					xs = ys = Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-xs") && argi < args.length) {
					xs = Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-ys") && argi < args.length) {
					ys = Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-z") && argi < args.length) {
					xs = ys = 1.0/Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-xz") && argi < args.length) {
					xs = 1.0/Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-yz") && argi < args.length) {
					ys = 1.0/Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-xt") && argi < args.length) {
					xt = Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("-yt") && argi < args.length) {
					yt = Double.parseDouble(args[argi++]);
				} else if (arg.equalsIgnoreCase("--help")) {
					printHelp(arg0);
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				System.out.println(rewriteSVGPath(arg, xs, ys, xt, yt));
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("RewriteSVGPath - Format and transform SVG paths.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] <paths>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -s  <scale>     Set horizontal and vertical scaling.");
		System.out.println("  -xs <scale>     Set horizontal scaling.");
		System.out.println("  -ys <scale>     Set vertical scaling.");
		System.out.println("  -xt <length>    Set horizontal translation.");
		System.out.println("  -yt <length>    Set vertical translation.");
		System.out.println();
	}
	
	private static enum ParamType {
		X_COORDINATE, Y_COORDINATE,
		X_DISPLACEMENT, Y_DISPLACEMENT,
		ANGLE, BOOLEAN_FLAG;
	}
	
	private static String rewriteSVGPath(String path, double xs, double ys, double xt, double yt) {
		String[] inPath = path
			.replaceAll(",", " ")
			.replaceAll("-", " -")
			.replaceAll("([Ee]) -", "$1-")
			.replaceAll("[A-DF-Za-df-z]", " $0 ")
			.trim()
			.split("\\s+");
		List<String> outPath = new ArrayList<String>();
		char currentCommand = 0;
		boolean currentRelative = false;
		int currentParamCount = 0;
		int currentParamNumber = 0;
		boolean first = true;
		for (String s : inPath) {
			if (s.length() > 0) {
				if (Character.isLetter(s.charAt(0))) {
					currentCommand = s.charAt(0);
					currentRelative = first ? false : Character.isLowerCase(currentCommand);
					currentParamCount = paramCount(currentCommand);
					currentParamNumber = 0;
					first = false;
					outPath.add(s);
				} else {
					if (currentParamNumber >= currentParamCount) {
						if (currentCommand == 'M') currentCommand = 'L';
						if (currentCommand == 'm') currentCommand = 'l';
						currentRelative = Character.isLowerCase(currentCommand);
						currentParamNumber = 0;
						outPath.add(Character.toString(currentCommand));
					}
					double v = Double.parseDouble(s);
					switch (paramType(currentCommand, currentParamNumber)) {
					case X_COORDINATE:
						if (!currentRelative) v += xt;
						// fall through
					case X_DISPLACEMENT:
						v *= xs;
						break;
					case Y_COORDINATE:
						if (!currentRelative) v += yt;
						// fall through
					case Y_DISPLACEMENT:
						v *= ys;
						break;
					case BOOLEAN_FLAG:
						v = ((v == 0) ? 0 : 1);
						break;
					default:
						break;
					}
					if (v == (int)v) {
						outPath.add(Integer.toString((int)v));
					} else {
						outPath.add(Double.toString(Math.round(v * 10000.0) / 10000.0));
					}
					currentParamNumber++;
				}
			}
		}
		StringBuffer sb = new StringBuffer();
		for (String s : outPath) {
			if (sb.length() > 0) sb.append(" ");
			sb.append(s);
		}
		return sb.toString();
	}
	
	private static int paramCount(char command) {
		switch (command) {
		case 'M': case 'm': return 2;
		case 'Z': case 'z': return 0;
		case 'L': case 'l': return 2;
		case 'H': case 'h': return 1;
		case 'V': case 'v': return 1;
		case 'C': case 'c': return 6;
		case 'S': case 's': return 4;
		case 'Q': case 'q': return 4;
		case 'T': case 't': return 2;
		case 'A': case 'a': return 7;
		default: return 0;
		}
	}
	
	private static ParamType paramType(char command, int index) {
		switch (command) {
		case 'H': case 'h':
			return ParamType.X_COORDINATE;
		case 'V': case 'v':
			return ParamType.Y_COORDINATE;
		case 'A': case 'a':
			switch (index % 7) {
			case 0: return ParamType.X_DISPLACEMENT;
			case 1: return ParamType.Y_DISPLACEMENT;
			case 2: return ParamType.ANGLE;
			case 3: return ParamType.BOOLEAN_FLAG;
			case 4: return ParamType.BOOLEAN_FLAG;
			case 5: return ParamType.X_COORDINATE;
			case 6: return ParamType.Y_COORDINATE;
			default: return null; // should not happen
			}
		default:
			switch (index % 2) {
			case 0: return ParamType.X_COORDINATE;
			case 1: return ParamType.Y_COORDINATE;
			default: return null; // should not happen
			}
		}
	}
}