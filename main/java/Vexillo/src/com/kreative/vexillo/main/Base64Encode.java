package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.kreative.vexillo.core.Base64OutputStream;

public class Base64Encode {
	public static void main(String[] args) {
		main(Vexillo.arg0(Base64Encode.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		boolean written = false;
		boolean pad = true;
		boolean opts = true;
		while (argi < args.length) {
			String arg = args[argi++];
			if (opts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					opts = false;
				} else if (arg.equals("-p")) {
					pad = true;
				} else if (arg.equals("-P")) {
					pad = false;
				} else if (arg.equals("-I")) {
					try {
						@SuppressWarnings("resource")
						Base64OutputStream out = new Base64OutputStream(System.out, pad);
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
					Base64OutputStream out = new Base64OutputStream(System.out, pad);
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
				Base64OutputStream out = new Base64OutputStream(System.out, pad);
				for (int b = System.in.read(); b >= 0; b = System.in.read()) out.write(b);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("Base64Encode - Encode files or standard input into Base64.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] [<files>]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -p  Use = for padding.");
		System.out.println("  -P  Do not use padding.");
		System.out.println("  -I  Read from standard input.");
		System.out.println("  -n  Insert newline in output.");
		System.out.println("  --  Treat remaining arguments as file names.");
		System.out.println();
		System.out.println("No arguments implies -p -I");
		System.out.println();
	}
}