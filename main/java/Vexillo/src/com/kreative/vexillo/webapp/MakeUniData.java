package com.kreative.vexillo.webapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import com.kreative.vexillo.font.CodeSequence;
import com.kreative.vexillo.font.Encoding;
import com.kreative.vexillo.font.EncodingNode;
import com.kreative.vexillo.main.Vexillo;

public class MakeUniData {
	public static void main(String[] args) throws IOException {
		main(Vexillo.arg0(MakeJS.class), args, 0);
	}
	
	public static void main(String arg0, String[] args, int argi) throws IOException {
		boolean parsingOptions = true;
		File outputFile = new File("unicodedata.txt");
		Encoding encoding = new Encoding();
		NameSet names = new NameSet();
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
				} else if (arg.equals("-n") && argi < args.length) {
					Scanner scanner = new Scanner(new File(args[argi++]));
					names.parse(scanner);
					scanner.close();
				} else {
					System.err.println("Unknown option: " + arg);
				}
			} else {
				System.err.println("Unknown option: " + arg);
			}
		}
		PrintWriter out = open(outputFile);
		out.println("@filename-txt FONTS/VEXILLO/UnicodeData.txt");
		out.println("@generate-txt FONTS/VEXILLO/UnicodeData.txt");
		for (int i = 0xE000; i < 0xF900; i++) process(i, encoding, names, out);
		for (int i = 0xF0000; i < 0xFFFFE; i++) process(i, encoding, names, out);
		for (int i = 0x100000; i < 0x10FFFE; i++) process(i, encoding, names, out);
		out.flush();
		out.close();
	}
	
	private static PrintWriter open(File file) throws IOException {
		FileOutputStream stream = new FileOutputStream(file);
		OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
		return new PrintWriter(writer, true);
	}
	
	private static void process(int cp, Encoding e, NameSet n, PrintWriter out) {
		CodeSequence key = new CodeSequence(new int[]{cp});
		EncodingNode en = e.getCodeSequenceMap().get(key);
		if (en == null) return;
		String name = getName(en, n);
		if (name == null) return;
		String compat = getCompat(en);
		if (compat == null) compat = "";
		String h = Integer.toHexString(cp).toUpperCase();
		while (h.length() < 4) h = "0" + h;
		out.println(h + ";" + name + ";So;0;ON;" + compat + ";;;;N;;;;;");
	}
	
	private static String getName(EncodingNode en, NameSet n) {
		String[] ids = en.toIdArray();
		for (String id : ids) {
			NameNode nn = n.getIdMap().get(id);
			if (nn != null) return nn.getName();
		}
		for (String id : ids) {
			return "FLAG FOR " + id.toUpperCase();
		}
		return null;
	}
	
	private static String getCompat(EncodingNode en) {
		CodeSequence seq = getCompatSequence(en);
		if (seq == null) return null;
		StringBuffer sb = new StringBuffer("<compat>");
		for (int cp : seq.toArray()) {
			sb.append(" ");
			String h = Integer.toHexString(cp).toUpperCase();
			for (int i = h.length(); i < 4; i++) sb.append("0");
			sb.append(h);
		}
		return sb.toString();
	}
	
	private static CodeSequence getCompatSequence(EncodingNode en) {
		// In order of preference:
		// RIS sequence
		for (CodeSequence seq : en.toCodeSequenceArray()) {
			int[] cp = seq.toArray();
			if (cp.length == 2 && isRIS(cp[0]) && isRIS(cp[1])) {
				return seq;
			}
		}
		// ZWJ sequence with U+1F3F4
		for (CodeSequence seq : en.toCodeSequenceArray()) {
			int[] cp = seq.toArray();
			if (cp.length > 2 && cp[0] == 0x1F3F4 && cp[1] == 0x200D) {
				return seq;
			}
		}
		// ZWJ sequence with U+1F3F3
		for (CodeSequence seq : en.toCodeSequenceArray()) {
			int[] cp = seq.toArray();
			if (cp.length > 2 && cp[0] == 0x1F3F3 && cp[1] == 0x200D) {
				return seq;
			}
		}
		// ZWJ sequence
		for (CodeSequence seq : en.toCodeSequenceArray()) {
			for (int cp : seq.toArray()) {
				if (cp == 0x200D) {
					return seq;
				}
			}
		}
		// SMP character (not a letter)
		looking: for (CodeSequence seq : en.toCodeSequenceArray()) {
			for (int cp : seq.toArray()) {
				if (cp < 0x10000 || isPUA(cp) || Character.isLetter(cp)) {
					continue looking;
				}
				return seq;
			}
		}
		// BMP character (not a letter)
		looking: for (CodeSequence seq : en.toCodeSequenceArray()) {
			for (int cp : seq.toArray()) {
				if (cp < 0x100 || isPUA(cp) || Character.isLetter(cp)) {
					continue looking;
				}
				return seq;
			}
		}
		return null;
	}
	
	private static boolean isRIS(int cp) {
		return (cp >= 0x1F1E6 && cp <= 0x1F1FF);
	}
	
	private static boolean isPUA(int cp) {
		return (
			(cp >= 0xE000 && cp < 0xF900) ||
			(cp >= 0xF0000 && cp < 0xFFFFE) ||
			(cp >= 0x100000 && cp < 0x10FFFE)
		);
	}
}