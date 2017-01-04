package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.kreative.vexillo.core.Base64InputStream;

public class Base64Decode {
	public static void main(String[] args) {
		main(Vexillo.arg0(Base64Decode.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		boolean written = false;
		boolean opts = true;
		while (argi < args.length) {
			String arg = args[argi++];
			if (opts && arg.startsWith("-")) {
				if (arg.equals("--")) {
					opts = false;
				} else if (arg.equals("-I")) {
					try {
						@SuppressWarnings("resource")
						Base64InputStream in = new Base64InputStream(System.in);
						for (int b = in.read(); b >= 0; b = in.read()) System.out.write(b);
						System.out.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
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
					Base64InputStream in = new Base64InputStream(new FileInputStream(new File(arg)));
					for (int b = in.read(); b >= 0; b = in.read()) System.out.write(b);
					System.out.flush();
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				written = true;
			}
		}
		if (!written) {
			try {
				@SuppressWarnings("resource")
				Base64InputStream in = new Base64InputStream(System.in);
				for (int b = in.read(); b >= 0; b = in.read()) System.out.write(b);
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("Base64Decode - Decode files or standard input from Base64.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] [<files>]");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -I  Read from standard input.");
		System.out.println("  --  Treat remaining arguments as file names.");
		System.out.println();
		System.out.println("No arguments implies -I");
		System.out.println();
	}
}