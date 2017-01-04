package com.kreative.vexillo.main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import com.kreative.vexillo.core.DimensionUtils;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.ImageUtils;
import com.kreative.vexillo.core.SVGExporter;

public class VexPort {
	public static void main(String[] args) {
		main(Vexillo.arg0(VexPort.class), args, 0);
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
				} else if (arg.equals("-w") && argi < args.length) {
					try { o.width = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.width = 0; }
				} else if (arg.equals("-h") && argi < args.length) {
					try { o.height = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.height = 0; }
				} else if (arg.equals("-s") && argi < args.length) {
					try { o.supersample = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.supersample = 0; }
				} else if (arg.equals("-g") && argi < args.length) {
					try { o.glaze = Integer.parseInt(args[argi++]); }
					catch (NumberFormatException e) { o.glaze = 0; }
				} else if (arg.equals("-f") && argi < args.length) {
					o.format = args[argi++];
				} else if (arg.equals("-o") && argi < args.length) {
					o.outputFile = new File(args[argi++]);
				} else if (arg.equals("-d") && argi < args.length) {
					o.outputDirectory = new File(args[argi++]);
				} else if (arg.equals("-v")) {
					o.verbose = true;
				} else if (arg.equals("--help")) {
					printHelp(arg0);
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				File inputFile = new File(arg);
				if (o.verbose) System.out.println(inputFile.getAbsolutePath() + " (" + o.getStatusString() + ")...");
				File outputFile = o.getOutputFile(inputFile);
				Flag flag = readFile(inputFile);
				if (flag != null) renderFlag(inputFile, flag, outputFile, o);
			}
		}
	}
	
	private static void printHelp(String arg0) {
		System.out.println();
		System.out.println("VexPort - Export Vexillo files to images.");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("  " + arg0 + " [<options>] <files>");
		System.out.println();
		System.out.println("Options:");
		System.out.println("  -w <width>      Set image width. Set to 0 to calculate from flag geometry.");
		System.out.println("  -h <height>     Set image height. Defaults to 200.");
		System.out.println("  -s <scale>      Render as a larger image, then reduce (supersampling).");
		System.out.println("  -g <thickness>  Add glazing like on FamFamFam flag icons. Set to 0 to disable.");
		System.out.println("  -f <format>     Set exported image format: png, jpg, gif, bmp, wbmp, or svg.");
		System.out.println("  -o <path>       Set output file. Forgotten once used.");
		System.out.println("  -d <path>       Set output directory.");
		System.out.println("  -v              Print paths of files as they are read (verbose).");
		System.out.println("  --              Treat remaining arguments as file names.");
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
	
	private static void renderFlag(File file, Flag flag, File out, Options o) {
		try {
			int height = o.height;
			if (height < 1) height = 200;
			int width = o.width;
			if (width < 1) width = (int)Math.round(flag.getFly().value(
				DimensionUtils.createNamespace(flag.dimensions(), height)));
			int glaze = o.glaze;
			if (o.format.equalsIgnoreCase("svg")) {
				PrintWriter pw = printToFile(out);
				if (pw == null) return;
				SVGExporter exporter = new SVGExporter(file.getParentFile(), flag);
				exporter.export(pw, width, height, glaze);
				pw.flush();
				pw.close();
			} else if (o.supersample > 1) {
				BufferedImage img = new BufferedImage(width * o.supersample, height * o.supersample, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();
				FlagRenderer renderer = new FlagRenderer(file.getParentFile(), flag);
				renderer.render(g, 0, 0, width * o.supersample, height * o.supersample);
				g.dispose();
				img = ImageUtils.scale(img, width, height);
				if (glaze > 0) {
					g = img.createGraphics();
					renderer.glaze(g, 0, 0, width, height, glaze);
					g.dispose();
				}
				writeFile(img, o.format, out);
			} else {
				BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g = img.createGraphics();
				FlagRenderer renderer = new FlagRenderer(file.getParentFile(), flag);
				renderer.render(g, 0, 0, width, height);
				if (glaze > 0) renderer.glaze(g, 0, 0, width, height, glaze);
				g.dispose();
				writeFile(img, o.format, out);
			}
		} catch (RuntimeException e) {
			System.err.println(
				"Error rendering " + file.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
		}
	}
	
	private static PrintWriter printToFile(File out) {
		try {
			FileOutputStream fos = new FileOutputStream(out);
			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
			PrintWriter pw = new PrintWriter(osw, true);
			return pw;
		} catch (IOException e) {
			System.err.println(
				"Error writing " + out.getName() + ": " +
				e.getClass().getSimpleName() + ": " +
				e.getMessage()
			);
			return null;
		}
	}
	
	private static void writeFile(BufferedImage img, String format, File out) {
		try {
			ImageIO.write(img, format, out);
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
		public int width = 0;
		public int height = 0;
		public int supersample = 0;
		public int glaze = 0;
		public String format = "png";
		public File outputFile = null;
		public File outputDirectory = null;
		public boolean verbose = false;
		
		public String getStatusString() {
			StringBuffer s = new StringBuffer();
			s.append((width > 0) ? (width + "x" + height) : height);
			if (supersample > 1) s.append(" " + supersample + "x");
			s.append((glaze > 0) ? " glazed" : " matte");
			s.append(" " + format);
			return s.toString();
		}
		
		public File getOutputFile(File inputFile) {
			if (this.outputFile != null) {
				File outputFile = this.outputFile;
				this.outputFile = null;
				return outputFile;
			} else if (outputDirectory != null) {
				String fileName = inputFile.getName();
				int period = fileName.lastIndexOf('.');
				if (period > 0) fileName = fileName.substring(0, period);
				return new File(outputDirectory, fileName + "." + format);
			} else {
				File parent = inputFile.getParentFile();
				String fileName = inputFile.getName();
				int period = fileName.lastIndexOf('.');
				if (period > 0) fileName = fileName.substring(0, period);
				return new File(parent, fileName + "." + format);
			}
		}
	}
}