package com.kreative.vexillo.main;

public class Vexillo {
	public static String arg0(Class<?> main) {
		try {
			String e = System.getProperty("com.kreative.arg0");
			if (e != null && e.length() > 0) return e;
		} catch (Exception e) {}
		
		String r = main.getSimpleName() + ".class";
		String u = main.getResource(r).toString();
		if (u.startsWith("jar:")) {
			u = u.substring(0, u.lastIndexOf('!'));
			u = u.substring(u.lastIndexOf('/') + 1);
			return "java -jar " + u;
		} else {
			return "java " + main.getCanonicalName();
		}
	}
	
	public static void main(String[] args) {
		main(Vexillo.arg0(Vexillo.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		if (argi >= args.length) {
			printHelp(arg0);
		} else if (args[argi].equalsIgnoreCase("view")) {
			VexView.main(arg0 + " view", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("export")) {
			VexPort.main(arg0 + " export", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("info")) {
			VexInfo.main(arg0 + " info", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("calc")) {
			VexCalc.main(arg0 + " calc", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("RewriteSVGPath")) {
			RewriteSVGPath.main(arg0 + " RewriteSVGPath", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("Base64Encode")) {
			Base64Encode.main(arg0 + " Base64Encode", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("Base64Decode")) {
			Base64Decode.main(arg0 + " Base64Decode", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("ASCII85Encode")) {
			ASCII85Encode.main(arg0 + " ASCII85Encode", args, argi + 1);
		} else if (args[argi].equalsIgnoreCase("ASCII85Decode")) {
			ASCII85Decode.main(arg0 + " ASCII85Decode", args, argi + 1);
		} else {
			printHelp(arg0);
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("Vexillo - Generate images of flags from XML descriptions.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " view <files>");
		System.out.println("  " + arg0 + " export [<options>] <files>");
		System.out.println("  " + arg0 + " info <fields> <files>");
		System.out.println("  " + arg0 + " calc <expression>");
		System.out.println("  " + arg0 + " RewriteSVGPath [<options>] <paths>");
		System.out.println("  " + arg0 + " Base64Encode [<options>] [<files>]");
		System.out.println("  " + arg0 + " Base64Decode [<options>] [<files>]");
		System.out.println("  " + arg0 + " ASCII85Encode [<options>] [<files>]");
		System.out.println("  " + arg0 + " ASCII85Decode [<options>] [<files>]");
		System.out.println();
	}
}