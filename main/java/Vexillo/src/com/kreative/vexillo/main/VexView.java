package com.kreative.vexillo.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.ui.DummyFrame;
import com.kreative.vexillo.ui.FlagFrame;
import com.kreative.vexillo.ui.OSUtils;

public class VexView {
	public static void main(String[] args) {
		main(Vexillo.arg0(VexView.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) {
		if (argi >= args.length) { printHelp(arg0); return; }
		boolean inited = false;
		while (argi < args.length) {
			String arg = args[argi++];
			if (arg.equals("--help")) {
				printHelp(arg0);
			} else {
				if (!inited) { init(); inited = true; }
				readFile(new File(arg));
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("VexView - View Vexillo files.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " <files>");
		System.out.println();
	}
	
	private static void init() {
		if (OSUtils.isMacOS()) {
			try { System.setProperty("apple.laf.useScreenMenuBar", "true"); } catch (Exception e) {}
			try { System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VexView"); } catch (Exception e) {}
			new DummyFrame();
		}
	}
	
	private static void readFile(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			Flag flag = FlagParser.parse(file.getName(), in);
			in.close();
			renderFlag(file, flag);
		} catch (IOException e) {
			System.err.println(
				"Error reading " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
		} catch (RuntimeException e) {
			System.err.println(
				"Error compiling " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			e.printStackTrace();
		}
	}
	
	private static void renderFlag(File file, Flag flag) {
		String title = file.getName();
		if (flag.getName() != null) title += ": " + flag.getName();
		FlagFrame frame = new FlagFrame(title, file, flag);
		frame.setVisible(true);
	}
}