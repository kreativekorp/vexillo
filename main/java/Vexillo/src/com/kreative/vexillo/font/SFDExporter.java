package com.kreative.vexillo.font;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.SVGExporter;

public class SFDExporter {
	private File parent;
	private String baseName;
	private File sfdFile;
	private FlagFontFamily font;
	private File cbdtDir;
	private File cbdtFile;
	private File sbixDir;
	private File svgDir;
	
	public SFDExporter(File baseFile, FlagFontFamily flagFontFamily) {
		parent = baseFile.getParentFile();
		baseName = baseFile.getName();
		int o = baseName.lastIndexOf('.');
		if (o > 0) baseName = baseName.substring(0, o);
		sfdFile = new File(parent, baseName + ".sfd");
		font = flagFontFamily;
	}
	
	public void includeCbdt() {
		cbdtDir = new File(parent, baseName + ".ttf.cbdt.d");
		cbdtDir = new File(cbdtDir, "0000");
		cbdtFile = new File(cbdtDir, "metadata.txt");
	}
	
	public void includeSbix() {
		sbixDir = new File(parent, baseName + ".ttf.sbix.d");
		sbixDir = new File(sbixDir, Integer.toString(font.getPixelsPerEm()));
	}
	
	public void includeSvg() {
		svgDir = new File(parent, baseName + ".ttf.svg.d");
	}
	
	public void export() throws IOException {
		boolean valid;
		FileOutputStream sfdfos = new FileOutputStream(sfdFile);
		OutputStreamWriter sfdosw = new OutputStreamWriter(sfdfos, "UTF-8");
		PrintWriter sfd = new PrintWriter(sfdosw, true);
		if (cbdtDir != null) cbdtDir.mkdirs();
		if (sbixDir != null) sbixDir.mkdirs();
		if (svgDir != null) svgDir.mkdirs();
		if (cbdtFile != null) {
			FileOutputStream cbdtfos = new FileOutputStream(cbdtFile);
			OutputStreamWriter cbdtosw = new OutputStreamWriter(cbdtfos, "UTF-8");
			PrintWriter cbdtmd = new PrintWriter(cbdtosw, true);
			valid = export(sfd, cbdtmd);
			cbdtmd.flush();
			cbdtmd.close();
		} else {
			valid = export(sfd, null);
		}
		sfd.flush();
		sfd.close();
		if (!valid) throw new IOException("numeric overflow in cbdt metrics");
	}
	
	private boolean export(PrintWriter sfd, PrintWriter cbdtmd) throws IOException {
		int nextPrivateUseCharacter = 0x100000;
		List<SFDCharacter> chars = new ArrayList<SFDCharacter>();
		chars.add(new SFDSpaceCharacter(0x20, chars.size(), font));
		chars.add(new SFDSpaceCharacter(0xA0, chars.size(), font));
		for (EncodingNode node : font.getNodes()) {
			CodeSequence ccs = node.getCanonicalCodeSequence();
			int cp = (ccs.length() == 1) ? ccs.codePointAt(0) : (nextPrivateUseCharacter++);
			Flag flag = font.getFlag(node);
			File flagFile = font.getFlagFile(node);
			chars.add(new SFDEmojiCharacter(cp, chars.size(), font, flag, node));
			if (cbdtDir != null) writeImage(cbdtDir, cp, font, flag, flagFile);
			if (sbixDir != null) writeImage(sbixDir, cp, font, flag, flagFile);
			if (svgDir != null) writeSVG(svgDir, cp, font, flag, flagFile);
			for (int i = 0; i < node.countCodeSequences(); i++) {
				CodeSequence cs = node.getCodeSequence(i);
				if (cs.length() == 1 && cs.codePointAt(0) != cp) {
					chars.add(new SFDEmojiCharacter(cs.codePointAt(0), chars.size(), font, flag, null));
					if (cbdtDir != null) writeImage(cbdtDir, cs.codePointAt(0), font, flag, flagFile);
					if (sbixDir != null) writeImage(sbixDir, cs.codePointAt(0), font, flag, flagFile);
					if (svgDir != null) writeSVG(svgDir, cs.codePointAt(0), font, flag, flagFile);
				}
			}
		}
		SortedSet<Integer> remaining = new TreeSet<Integer>();
		remaining.add(0x1F3F3);
		remaining.add(0x1F3F4);
		for (char ch = '0'; ch <= '9'; ch++) remaining.add(0xE0000 + ch);
		for (char ch = 'a'; ch <= 'z'; ch++) remaining.add(0xE0000 + ch);
		remaining.add(0xE007F);
		for (EncodingNode node : font.getNodes()) {
			for (int i = 0; i < node.countCodeSequences(); i++) {
				CodeSequence cs = node.getCodeSequence(i);
				for (int j = 0; j < cs.length(); j++) {
					remaining.add(cs.codePointAt(j));
				}
			}
		}
		for (SFDCharacter ch : chars) remaining.remove(ch.cp);
		for (int cp : remaining) chars.add(new SFDEmptyCharacter(cp, chars.size()));
		printFont(sfd, font, chars);
		return cbdtmd == null || printCbdtMetadata(cbdtmd, font, chars);
	}
	
	private static void printFont(PrintWriter sfd, FlagFontFamily font, List<SFDCharacter> chars) {
		sfd.println("SplineFontDB: 3.0");
		sfd.println("FontName: " + font.name.replaceAll("[^A-Za-z0-9]+", ""));
		sfd.println("FullName: " + font.name);
		sfd.println("FamilyName: " + font.name);
		sfd.println("Weight: Medium");
		sfd.println("Copyright: " + font.copyright);
		sfd.println("Version: 1.0");
		sfd.println("ItalicAngle: 0");
		sfd.println("UnderlinePosition: -100");
		sfd.println("UnderlineWidth: 100");
		sfd.println("Ascent: " + font.emAscent);
		sfd.println("Descent: " + font.emDescent);
		sfd.println("LayerCount: 2");
		sfd.println("Layer: 0 0 \"Back\" 1");
		sfd.println("Layer: 1 0 \"Fore\" 0");
		sfd.println("OS2Version: 0");
		sfd.println("OS2_WeightWidthSlopeOnly: 0");
		sfd.println("OS2_UseTypoMetrics: 1");
		sfd.println("PfmFamily: 81");
		sfd.println("TTFWeight: 500");
		sfd.println("TTFWidth: 5");
		sfd.println("LineGap: 0");
		sfd.println("VLineGap: 0");
		sfd.println("OS2TypoAscent: " + font.lineAscent);
		sfd.println("OS2TypoAOffset: 0");
		sfd.println("OS2TypoDescent: " + -font.lineDescent);
		sfd.println("OS2TypoDOffset: 0");
		sfd.println("OS2TypoLinegap: 0");
		sfd.println("OS2WinAscent: " + font.lineAscent);
		sfd.println("OS2WinAOffset: 0");
		sfd.println("OS2WinDescent: " + font.lineDescent);
		sfd.println("OS2WinDOffset: 0");
		sfd.println("HheadAscent: " + font.lineAscent);
		sfd.println("HheadAOffset: 0");
		sfd.println("HheadDescent: " + -font.lineDescent);
		sfd.println("HheadDOffset: 0");
		sfd.println("OS2Vendor: \'" + font.vendorId + "\'");
		sfd.println("Lookup: 4 0 1 \"Emoji Sequences 0\" {\"Emoji Sequences 0-1\"} [<1,0> ('DFLT' <'dflt'>) 'rlig' ('DFLT' <'dflt'>)]");
		sfd.println("DEI: 91125");
		sfd.println("Encoding: UnicodeFull");
		sfd.println("UnicodeInterp: none");
		sfd.println("NameList: Adobe Glyph List");
		sfd.println("DisplaySize: -24");
		sfd.println("AntiAlias: 1");
		sfd.println("FitToEm: 1");
		sfd.println("BeginChars: 1114112 " + chars.size());
		sfd.println();
		for (SFDCharacter ch : chars) {
			ch.print(sfd);
			sfd.println();
		}
		sfd.println("EndChars");
		sfd.println("EndSplineFont");
	}
	
	private static boolean printCbdtMetadata(PrintWriter out, FlagFontFamily font, List<SFDCharacter> chars) {
		boolean valid = true;
		int ppem = font.getPixelsPerEm();
		int ascent = font.emUnitsToPixels(font.lineAscent);
		int descent = font.emUnitsToPixels(-font.lineDescent);
		int widthMax = 0;
		for (SFDCharacter ch : chars) {
			if (ch instanceof SFDEmojiCharacter) {
				int width = font.emUnitsToPixels(ch.width);
				if (width > widthMax) widthMax = width;
			}
		}
		if (ppem < 0 || ppem >= 256) valid = false;
		if (ascent < -128 || ascent >= 128) valid = false;
		if (descent < -128 || descent >= 128) valid = false;
		if (widthMax < 0 || widthMax >= 256) valid = false;
		out.println("horiAscender: " + ascent);
		out.println("horiDescender: " + descent);
		out.println("horiWidthMax: " + widthMax);
		out.println("vertAscender: " + ascent);
		out.println("vertDescender: " + descent);
		out.println("vertWidthMax: " + widthMax);
		out.println("ppemX: " + ppem);
		out.println("ppemY: " + ppem);
		for (SFDCharacter ch : chars) {
			if (ch instanceof SFDEmojiCharacter) {
				SFDEmojiCharacter ech = (SFDEmojiCharacter)ch;
				String h = Integer.toHexString(ech.cp).toUpperCase();
				int height = font.emUnitsToPixels(ech.y2 - ech.y1);
				int width = font.emUnitsToPixels(ech.x2 - ech.x1);
				int bearingX = font.emUnitsToPixels(ech.x1);
				int bearingY = font.emUnitsToPixels(ech.y2);
				int advance = font.emUnitsToPixels(ech.width);
				if (height < 0 || height >= 256) valid = false;
				if (width < 0 || width >= 256) valid = false;
				if (bearingX < -128 || bearingX >= 128) valid = false;
				if (bearingY < -128 || bearingY >= 128) valid = false;
				if (advance < 0 || advance >= 256) valid = false;
				out.println();
				out.println("glyph: char_" + h);
				out.println("height: " + height);
				out.println("width: " + width);
				out.println("bearingX: " + bearingX);
				out.println("bearingY: " + bearingY);
				out.println("advance: " + advance);
				out.println("endGlyph");
			}
		}
		return valid;
	}
	
	private static void writeImage(File sbixDir, int cp, FlagFontFamily font, Flag flag, File flagFile) throws IOException {
		FlagRenderer renderer = new FlagRenderer(flagFile.getParentFile(), flag);
		File pngFile = new File(sbixDir, "char_" + Integer.toHexString(cp).toUpperCase() + ".png");
		int w = (font.bitmapWidth > 0) ? font.bitmapWidth : flag.getWidthFromHeight(font.bitmapHeight);
		if (font.bitmapStyle == null) renderer.renderToFile(pngFile, "png", w, font.bitmapHeight, 0, font.bitmapGlaze);
		else ImageIO.write(font.bitmapStyle.stylize(renderer, w, font.bitmapHeight, 0, font.bitmapGlaze), "png", pngFile);
	}
	
	private static void writeSVG(File svgDir, int cp, FlagFontFamily font, Flag flag, File flagFile) throws IOException {
		int tx = font.leftBearing;
		int ty = font.glyphBottom + font.glyphHeight;
		File svgFile = new File(svgDir, "char_" + Integer.toHexString(cp).toUpperCase() + ".svg");
		int w = (font.glyphWidth > 0) ? font.glyphWidth : flag.getWidthFromHeight(font.glyphHeight);
		if (font.bitmapStyle == null) {
			SVGExporter exporter = new SVGExporter(flagFile.getParentFile(), flag, true, tx, -ty);
			int g = font.glyphHeight * font.bitmapGlaze / font.bitmapHeight;
			exporter.export(svgFile, w, font.glyphHeight, g);
		} else {
			FlagRenderer renderer = new FlagRenderer(flagFile.getParentFile(), flag);
			int iw = (font.bitmapWidth > 0) ? font.bitmapWidth : flag.getWidthFromHeight(font.bitmapHeight);
			BufferedImage image = font.bitmapStyle.stylize(renderer, iw, font.bitmapHeight, 0, font.bitmapGlaze);
			ImageSVGExporter.exportToFile(image, "png", "image/png", tx, -ty, w, font.glyphHeight, svgFile);
		}
	}
	
	private static abstract class SFDCharacter {
		protected int cp, index, width;
		protected void printContents(PrintWriter sfd) {}
		public final void print(PrintWriter sfd) {
			sfd.println("StartChar: " + getCharacterName(cp));
			sfd.println("Encoding: " + cp + " " + cp + " " + index);
			sfd.println("Width: " + width);
			sfd.println("VWidth: 0");
			sfd.println("Flags: HW");
			sfd.println("LayerCount: 2");
			sfd.println("Back");
			sfd.println("Fore");
			printContents(sfd);
			sfd.println("EndChar");
		}
	}
	
	private static final class SFDSpaceCharacter extends SFDCharacter {
		public SFDSpaceCharacter(int cp, int index, FlagFontFamily font) {
			this.cp = cp;
			this.index = index;
			this.width = font.spaceWidth;
		}
	}
	
	private static final class SFDEmojiCharacter extends SFDCharacter {
		private final int x1, x2, y1, y2;
		private final CodeSequence[] seqs;
		private final String[] ids;
		public SFDEmojiCharacter(int cp, int index, FlagFontFamily font, Flag flag, EncodingNode node) {
			int w = (font.glyphWidth > 0) ? font.glyphWidth : flag.getWidthFromHeight(font.glyphHeight);
			this.cp = cp;
			this.index = index;
			this.width = font.leftBearing + w + font.rightBearing;
			this.x1 = font.leftBearing;
			this.x2 = font.leftBearing + w;
			this.y1 = font.glyphBottom;
			this.y2 = font.glyphBottom + font.glyphHeight;
			this.seqs = (node == null) ? null : node.toCodeSequenceArray();
			this.ids = (node == null) ? null : node.toNormalizedIdArray();
		}
		@Override
		protected void printContents(PrintWriter sfd) {
			sfd.println("SplineSet");
			sfd.println(" " + x1 + " " + y1 + " m 1");
			sfd.println(" " + x1 + " " + y2 + " l 1");
			sfd.println(" " + x2 + " " + y2 + " l 1");
			sfd.println(" " + x2 + " " + y1 + " l 1");
			sfd.println(" " + x1 + " " + y1 + " l 1");
			sfd.println("EndSplineSet");
			if (seqs != null) {
				for (CodeSequence seq : seqs) {
					if (seq.length() > 1) {
						StringBuffer sb = new StringBuffer("Ligature2: \"Emoji Sequences 0-1\"");
						for (int i = 0; i < seq.length(); i++) {
							sb.append(' ');
							sb.append(getCharacterName(seq.codePointAt(i)));
						}
						sfd.println(sb.toString());
					}
				}
			}
			if (ids != null) {
				for (String id : ids) {
					if (id.length() > 0) {
						// Proposal for flag tag sequences used 1F3F3 as base.
						StringBuffer sb = new StringBuffer("Ligature2: \"Emoji Sequences 0-1\"");
						for (int codePoint : getFlagIdCodeSequence(id, 0x1F3F3)) {
							sb.append(' ');
							sb.append(getCharacterName(codePoint));
						}
						sfd.println(sb.toString());
						// Final version of flag tag sequences uses 1F3F4 as base.
						sb = new StringBuffer("Ligature2: \"Emoji Sequences 0-1\"");
						for (int codePoint : getFlagIdCodeSequence(id, 0x1F3F4)) {
							sb.append(' ');
							sb.append(getCharacterName(codePoint));
						}
						sfd.println(sb.toString());
					}
				}
			}
		}
	}
	
	private static final class SFDEmptyCharacter extends SFDCharacter {
		public SFDEmptyCharacter(int cp, int index) {
			this.cp = cp;
			this.index = index;
			this.width = 0;
		}
	}
	
	private static String getCharacterName(int cp) {
		switch (cp) {
			case 0x20: return "space";          case 0x21: return "exclam";
			case 0x22: return "quotedbl";       case 0x23: return "numbersign";
			case 0x24: return "dollar";         case 0x25: return "percent";
			case 0x26: return "ampersand";      case 0x27: return "quotesingle";
			case 0x28: return "parenleft";      case 0x29: return "parenright";
			case 0x2A: return "asterisk";       case 0x2B: return "plus";
			case 0x2C: return "comma";          case 0x2D: return "hyphen";
			case 0x2E: return "period";         case 0x2F: return "slash";
			case 0x30: return "zero";           case 0x31: return "one";
			case 0x32: return "two";            case 0x33: return "three";
			case 0x34: return "four";           case 0x35: return "five";
			case 0x36: return "six";            case 0x37: return "seven";
			case 0x38: return "eight";          case 0x39: return "nine";
			case 0x3A: return "colon";          case 0x3B: return "semicolon";
			case 0x3C: return "less";           case 0x3D: return "equal";
			case 0x3E: return "greater";        case 0x3F: return "question";
			case 0x40: return "at";             case 0x41: return "A";
			case 0x42: return "B";              case 0x43: return "C";
			case 0x44: return "D";              case 0x45: return "E";
			case 0x46: return "F";              case 0x47: return "G";
			case 0x48: return "H";              case 0x49: return "I";
			case 0x4A: return "J";              case 0x4B: return "K";
			case 0x4C: return "L";              case 0x4D: return "M";
			case 0x4E: return "N";              case 0x4F: return "O";
			case 0x50: return "P";              case 0x51: return "Q";
			case 0x52: return "R";              case 0x53: return "S";
			case 0x54: return "T";              case 0x55: return "U";
			case 0x56: return "V";              case 0x57: return "W";
			case 0x58: return "X";              case 0x59: return "Y";
			case 0x5A: return "Z";              case 0x5B: return "bracketleft";
			case 0x5C: return "backslash";      case 0x5D: return "bracketright";
			case 0x5E: return "asciicircum";    case 0x5F: return "underscore";
			case 0x60: return "grave";          case 0x61: return "a";
			case 0x62: return "b";              case 0x63: return "c";
			case 0x64: return "d";              case 0x65: return "e";
			case 0x66: return "f";              case 0x67: return "g";
			case 0x68: return "h";              case 0x69: return "i";
			case 0x6A: return "j";              case 0x6B: return "k";
			case 0x6C: return "l";              case 0x6D: return "m";
			case 0x6E: return "n";              case 0x6F: return "o";
			case 0x70: return "p";              case 0x71: return "q";
			case 0x72: return "r";              case 0x73: return "s";
			case 0x74: return "t";              case 0x75: return "u";
			case 0x76: return "v";              case 0x77: return "w";
			case 0x78: return "x";              case 0x79: return "y";
			case 0x7A: return "z";              case 0x7B: return "braceleft";
			case 0x7C: return "bar";            case 0x7D: return "braceright";
			case 0x7E: return "asciitilde";     case 0xA1: return "exclamdown";
			case 0xA2: return "cent";           case 0xA3: return "sterling";
			case 0xA4: return "currency";       case 0xA5: return "yen";
			case 0xA6: return "brokenbar";      case 0xA7: return "section";
			case 0xA8: return "dieresis";       case 0xA9: return "copyright";
			case 0xAA: return "ordfeminine";    case 0xAB: return "guillemotleft";
			case 0xAC: return "logicalnot";     case 0xAE: return "registered";
			case 0xAF: return "macron";         case 0xB0: return "degree";
			case 0xB1: return "plusminus";      case 0xB4: return "acute";
			case 0xB5: return "mu";             case 0xB6: return "paragraph";
			case 0xB7: return "periodcentered"; case 0xB8: return "cedilla";
			case 0xBA: return "ordmasculine";   case 0xBB: return "guillemotright";
			case 0xBC: return "onequarter";     case 0xBD: return "onehalf";
			case 0xBE: return "threequarters";  case 0xBF: return "questiondown";
			case 0xC0: return "Agrave";         case 0xC1: return "Aacute";
			case 0xC2: return "Acircumflex";    case 0xC3: return "Atilde";
			case 0xC4: return "Adieresis";      case 0xC5: return "Aring";
			case 0xC6: return "AE";             case 0xC7: return "Ccedilla";
			case 0xC8: return "Egrave";         case 0xC9: return "Eacute";
			case 0xCA: return "Ecircumflex";    case 0xCB: return "Edieresis";
			case 0xCC: return "Igrave";         case 0xCD: return "Iacute";
			case 0xCE: return "Icircumflex";    case 0xCF: return "Idieresis";
			case 0xD0: return "Eth";            case 0xD1: return "Ntilde";
			case 0xD2: return "Ograve";         case 0xD3: return "Oacute";
			case 0xD4: return "Ocircumflex";    case 0xD5: return "Otilde";
			case 0xD6: return "Odieresis";      case 0xD7: return "multiply";
			case 0xD8: return "Oslash";         case 0xD9: return "Ugrave";
			case 0xDA: return "Uacute";         case 0xDB: return "Ucircumflex";
			case 0xDC: return "Udieresis";      case 0xDD: return "Yacute";
			case 0xDE: return "Thorn";          case 0xDF: return "germandbls";
			case 0xE0: return "agrave";         case 0xE1: return "aacute";
			case 0xE2: return "acircumflex";    case 0xE3: return "atilde";
			case 0xE4: return "adieresis";      case 0xE5: return "aring";
			case 0xE6: return "ae";             case 0xE7: return "ccedilla";
			case 0xE8: return "egrave";         case 0xE9: return "eacute";
			case 0xEA: return "ecircumflex";    case 0xEB: return "edieresis";
			case 0xEC: return "igrave";         case 0xED: return "iacute";
			case 0xEE: return "icircumflex";    case 0xEF: return "idieresis";
			case 0xF0: return "eth";            case 0xF1: return "ntilde";
			case 0xF2: return "ograve";         case 0xF3: return "oacute";
			case 0xF4: return "ocircumflex";    case 0xF5: return "otilde";
			case 0xF6: return "odieresis";      case 0xF7: return "divide";
			case 0xF8: return "oslash";         case 0xF9: return "ugrave";
			case 0xFA: return "uacute";         case 0xFB: return "ucircumflex";
			case 0xFC: return "udieresis";      case 0xFD: return "yacute";
			case 0xFE: return "thorn";          case 0xFF: return "ydieresis";
		}
		if (cp >= 0 && cp < 0x10000) {
			String h = "0000" + Integer.toHexString(cp).toUpperCase();
			return "uni" + h.substring(h.length() - 4);
		} else {
			return "u" + Integer.toHexString(cp).toUpperCase();
		}
	}
	
	private static int[] getFlagIdCodeSequence(String id, int base) {
		int[] buf = new int[id.length() + 2];
		int pos = 0;
		buf[pos++] = base;
		CharacterIterator it = new StringCharacterIterator(id);
		for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
			if ((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'z')) {
				buf[pos++] = 0xE0000 + ch;
			} else if (ch >= 'A' && ch <= 'Z') {
				buf[pos++] = 0xE0020 + ch;
			}
		}
		buf[pos++] = 0xE007F;
		int[] codePoints = new int[pos];
		for (int i = 0; i < pos; i++) {
			codePoints[i] = buf[i];
		}
		return codePoints;
	}
}