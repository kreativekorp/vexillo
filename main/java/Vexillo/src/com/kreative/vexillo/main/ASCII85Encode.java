package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.kreative.vexillo.core.ASCII85OutputStream;

public class ASCII85Encode {
	public static void main(String[] args) {
		main(Vexillo.arg0(ASCII85Encode.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		boolean written = false;
		boolean xml = false;
		boolean x = false;
		boolean y = false;
		boolean z = true;
		boolean opts = true;
		while (argi < args.length) {
			String arg = args[argi++];
			if (opts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					opts = false;
				} else if (arg.equals("-x")) {
					x = true;
				} else if (arg.equals("-X")) {
					x = false;
				} else if (arg.equals("-y")) {
					y = true;
				} else if (arg.equals("-Y")) {
					y = false;
				} else if (arg.equals("-z")) {
					z = true;
				} else if (arg.equals("-Z")) {
					z = false;
				} else if (arg.equals("-m")) {
					xml = true;
				} else if (arg.equals("-M")) {
					xml = false;
				} else if (arg.equals("-I")) {
					try {
						@SuppressWarnings("resource")
						ASCII85OutputStream out = new ASCII85OutputStream(System.out, xml, x, y, z);
						for (int b = System.in.read(); b >= 0; b = System.in.read()) out.write(b);
						out.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					written = true;
				} else if (arg.equals("-n")) {
					System.out.write(0x0A);
					written = true;
				} else if (arg.equals("--help")) {
					printHelp(arg0);
					written = true;
				} else {
					System.err.println("Unknown option: " + arg);
					written = true;
				}
			} else {
				try {
					@SuppressWarnings("resource")
					ASCII85OutputStream out = new ASCII85OutputStream(System.out, xml, x, y, z);
					InputStream in = new FileInputStream(new File(arg));
					for (int b = in.read(); b >= 0; b = in.read()) out.write(b);
					in.close();
					out.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				written = true;
			}
		}
		if (!written) {
			try {
				@SuppressWarnings("resource")
				ASCII85OutputStream out = new ASCII85OutputStream(System.out, xml, x, y, z);
				for (int b = System.in.read(); b >= 0; b = System.in.read()) out.write(b);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("ASCII85Encode - Encode files or standard input into ASCII85.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] [<files>]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -z  Use z for 0x00000000.");
		System.out.println("  -Z  Do not use z for 0x00000000.");
		System.out.println("  -y  Use y for 0x20202020.");
		System.out.println("  -Y  Do not use y for 0x20202020.");
		System.out.println("  -x  Use x for 0xFFFFFFFF.");
		System.out.println("  -X  Do not use x for 0xFFFFFFFF.");
		System.out.println("  -m  Replace \' \" & < > with v w | { } (XML-safe encoding).");
		System.out.println("  -M  Only use \' \" & < > (standard ASCII85 encoding).");
		System.out.println("  -I  Read from standard input.");
		System.out.println("  -n  Insert newline in output.");
		System.out.println("  --  Treat remaining arguments as file names.");
		System.out.println();
		System.out.println("No arguments implies -z -Y -X -M -I");
		System.out.println();
	}
}