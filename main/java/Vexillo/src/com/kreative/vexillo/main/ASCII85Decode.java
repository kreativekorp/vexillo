package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.kreative.vexillo.core.ASCII85InputStream;

public class ASCII85Decode {
	public static void main(String[] args) {
		main(Vexillo.arg0(ASCII85Decode.class), args, 0);
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
						ASCII85InputStream in = new ASCII85InputStream(System.in);
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
					ASCII85InputStream in = new ASCII85InputStream(new FileInputStream(new File(arg)));
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
				ASCII85InputStream in = new ASCII85InputStream(System.in);
				for (int b = in.read(); b >= 0; b = in.read()) System.out.write(b);
				System.out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("ASCII85Decode - Decode files or standard input from ASCII85.");
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