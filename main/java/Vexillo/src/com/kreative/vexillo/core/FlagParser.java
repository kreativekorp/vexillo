package com.kreative.vexillo.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FlagParser {
	public static Flag parse(String name, InputStream in) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true); // make sure the XML is valid
			factory.setExpandEntityReferences(false); // don't allow custom entities
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(new KVXXEntityResolver());
			builder.setErrorHandler(new KVXXErrorHandler(name));
			Document document = builder.parse(new InputSource(in));
			return parseDocument(document);
		} catch (ParserConfigurationException pce) {
			throw new IOException(pce);
		} catch (SAXException saxe) {
			throw new IOException(saxe);
		}
	}
	
	private static Flag parseDocument(Node node) throws IOException {
		String type = node.getNodeName();
		if (type.equalsIgnoreCase("#document")) {
			for (Node child : getChildren(node)) {
				String ctype = child.getNodeName();
				if (ctype.equalsIgnoreCase("flag")) {
					if (child.hasAttributes() || child.hasChildNodes()) {
						return parseFlag(child);
					}
				} else {
					throw new IOException("Unknown element: " + ctype);
				}
			}
			throw new IOException("Empty document.");
		} else {
			throw new IOException("Unknown element: " + type);
		}
	}
	
	private static Flag parseFlag(Node node) throws IOException {
		String type = node.getNodeName();
		if (type.equalsIgnoreCase("flag")) {
			Flag flag = new Flag();
			NamedNodeMap attr = node.getAttributes();
			String id = parseString(attr, "id");
			String name = parseString(attr, "name");
			PropertySet properties = parseProperties(attr, "properties");
			Dimension fly = parseDimension(attr, "fly");
			if (id != null) flag.setId(id);
			if (name != null) flag.setName(name);
			if (properties != null) flag.setProperties(properties);
			if (fly != null) flag.setFly(fly);
			for (Node child : getChildren(node)) {
				String ctype = child.getNodeName();
				if (ctype.equalsIgnoreCase("defs")) {
					parseDefs(flag, child);
				} else {
					parseInstruction(flag.instructions(), child, false);
				}
			}
			return flag;
		} else {
			throw new IOException("Unknown element: " + type);
		}
	}
	
	private static void parseDefs(Flag flag, Node node) throws IOException {
		String type = node.getNodeName();
		if (type.equalsIgnoreCase("defs")) {
			for (Node child : getChildren(node)) {
				String ctype = child.getNodeName();
				if (ctype.equalsIgnoreCase("dim")) {
					NamedNodeMap cattr = child.getAttributes();
					String id = parseString(cattr, "id");
					Dimension e = parseDimension(cattr, "e");
					flag.dimensions().put(id, e);
				} else if (ctype.equalsIgnoreCase("color")) {
					NamedNodeMap cattr = child.getAttributes();
					String id = parseString(cattr, "id");
					Color.Multi color = new Color.Multi();
					for (Node gc : getChildren(child)) {
						String gctype = gc.getNodeName();
						if (gctype.equalsIgnoreCase("colorspec")) {
							NamedNodeMap gcattr = gc.getAttributes();
							String model = parseString(gcattr, "model");
							String value = parseString(gcattr, "value");
							color.add(ColorParser.parse(model, value));
						} else {
							throw new IOException("Unknown element: " + gctype);
						}
					}
					flag.colors().put(id, color);
				} else if (ctype.equalsIgnoreCase("symdef")) {
					NamedNodeMap cattr = child.getAttributes();
					String id = parseString(cattr, "id");
					String d = parseString(cattr, "d");
					flag.symbols().put(id, new Symbol(d));
				} else if (ctype.equalsIgnoreCase("imgdef")) {
					NamedNodeMap cattr = child.getAttributes();
					String id = parseString(cattr, "id");
					Image image = new Image();
					for (Node gc : getChildren(child)) {
						String gctype = gc.getNodeName();
						if (gctype.equalsIgnoreCase("imgsrc")) {
							NamedNodeMap gcattr = gc.getAttributes();
							String imgtype = parseString(gcattr, "type");
							ImageSource.Encoding imgenc = parseImageSourceEncoding(gcattr, "enc");
							String imgsrc = parseString(gcattr, "src");
							String imgdata = trimLines(gc.getTextContent());
							image.add(new ImageSource(imgtype, imgenc, imgsrc, imgdata));
						} else {
							throw new IOException("Unknown element: " + gctype);
						}
					}
					flag.images().put(id, image);
				} else {
					throw new IOException("Unknown element: " + ctype);
				}
			}
		} else {
			throw new IOException("Unknown element: " + type);
		}
	}
	
	private static void parseInstruction(List<Instruction> list, Node node, boolean clip) throws IOException {
		String type = node.getNodeName();
		if (type.equalsIgnoreCase("g")) {
			Instruction.GroupInstruction block = new Instruction.GroupInstruction();
			for (Node child : getChildren(node)) {
				String ctype = child.getNodeName();
				if (ctype.equalsIgnoreCase("clip")) {
					for (Node gc : getChildren(child)) {
						parseInstruction(block.clippingRegion, gc, true);
					}
				} else {
					parseInstruction(block.instructions, child, clip);
				}
			}
			list.add(block);
		} else if (type.equalsIgnoreCase("for")) {
			NamedNodeMap attr = node.getAttributes();
			String var = parseString(attr, "var");
			Dimension start = parseDimension(attr, "start");
			Dimension end = parseDimension(attr, "end");
			Dimension step = parseDimension(attr, "step");
			if (step == null) step = new Dimension.Constant(1);
			Instruction.ForInstruction block = new Instruction.ForInstruction(var, start, end, step);
			for (Node child : getChildren(node)) {
				parseInstruction(block.instructions, child, clip);
			}
			list.add(block);
		} else if (type.equalsIgnoreCase("field")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.FieldInstruction(x1, y1, x2, y2, color));
		} else if (type.equalsIgnoreCase("hband")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			int bands = parseInt(attr, "bands", 1);
			String[] colors = clip ? new String[]{"clip"} : parseStringArray(attr, "colors");
			int[] weights = parseIntArray(attr, "weights", 1);
			Instruction.HBandInstruction hband = new Instruction.HBandInstruction(x1, y1, x2, y2, bands);
			if (colors != null) for (String color : colors) hband.bandColors.add(color);
			if (weights != null) for (int weight : weights) hband.bandWeights.add(weight);
			list.add(hband);
		} else if (type.equalsIgnoreCase("vband")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			int bands = parseInt(attr, "bands", 1);
			String[] colors = clip ? new String[]{"clip"} : parseStringArray(attr, "colors");
			int[] weights = parseIntArray(attr, "weights", 1);
			Instruction.VBandInstruction vband = new Instruction.VBandInstruction(x1, y1, x2, y2, bands);
			if (colors != null) for (String color : colors) vband.bandColors.add(color);
			if (weights != null) for (int weight : weights) vband.bandWeights.add(weight);
			list.add(vband);
		} else if (type.equalsIgnoreCase("cross")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			Dimension x3 = parseDimension(attr, "x3");
			Dimension y3 = parseDimension(attr, "y3");
			Dimension x4 = parseDimension(attr, "x4");
			Dimension y4 = parseDimension(attr, "y4");
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.CrossInstruction(x1, y1, x2, y2, x3, y3, x4, y4, color));
		} else if (type.equalsIgnoreCase("saltire")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			Dimension thickness = parseDimension(attr, "thickness");
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.SaltireInstruction(x1, y1, x2, y2, thickness, color));
		} else if (type.equalsIgnoreCase("dband")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			Dimension thickness = parseDimension(attr, "thickness");
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.DBandInstruction(x1, y1, x2, y2, thickness, color));
		} else if (type.equalsIgnoreCase("disc")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension cx = parseDimension(attr, "cx");
			Dimension cy = parseDimension(attr, "cy");
			Dimension w = parseDimension(attr, "w");
			Dimension h = parseDimension(attr, "h");
			Dimension arcStart = parseDimension(attr, "arcstart");
			if (arcStart == null) arcStart = new Dimension.Constant(0);
			Dimension arcEnd = parseDimension(attr, "arcend");
			if (arcEnd == null) arcEnd = new Dimension.Constant(360);
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.DiscInstruction(cx, cy, w, h, arcStart, arcEnd, color));
		} else if (type.equalsIgnoreCase("poly")) {
			NamedNodeMap attr = node.getAttributes();
			String points = parseString(attr, "points");
			DimensionParser pp = new DimensionParser(points);
			List<Dimension> xd = new ArrayList<Dimension>();
			List<Dimension> yd = new ArrayList<Dimension>();
			int np = 0;
			while (pp.hasNext()) {
				xd.add(pp.parseNext());
				yd.add(pp.parseNext());
				np++;
			}
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.PolyInstruction(
				np, xd.toArray(new Dimension[0]),
				yd.toArray(new Dimension[0]), color
			));
		} else if (type.equalsIgnoreCase("symbol")) {
			NamedNodeMap attr = node.getAttributes();
			String symbol = parseString(attr, "symbol");
			Dimension x = parseDimension(attr, "x");
			Dimension y = parseDimension(attr, "y");
			Dimension sx = parseDimension(attr, "sx");
			Dimension sy = parseDimension(attr, "sy");
			Dimension rotate = parseDimension(attr, "rotate");
			if (rotate == null) rotate = new Dimension.Constant(0);
			String color = clip ? "clip" : parseString(attr, "color");
			list.add(new Instruction.SymbolInstruction(symbol, x, y, sx, sy, rotate, color));
		} else if (type.equalsIgnoreCase("image")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			String image = parseString(attr, "image");
			list.add(new Instruction.ImageInstruction(x1, y1, x2, y2, image));
		} else if (type.equalsIgnoreCase("hgrad")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			String color1 = clip ? "clip" : parseString(attr, "color1");
			String color2 = clip ? "clip" : parseString(attr, "color2");
			list.add(new Instruction.HGradInstruction(x1, y1, x2, y2, color1, color2));
		} else if (type.equalsIgnoreCase("vgrad")) {
			NamedNodeMap attr = node.getAttributes();
			Dimension x1 = parseDimension(attr, "x1");
			Dimension y1 = parseDimension(attr, "y1");
			Dimension x2 = parseDimension(attr, "x2");
			Dimension y2 = parseDimension(attr, "y2");
			String color1 = clip ? "clip" : parseString(attr, "color1");
			String color2 = clip ? "clip" : parseString(attr, "color2");
			list.add(new Instruction.VGradInstruction(x1, y1, x2, y2, color1, color2));
		} else {
			throw new IOException("Unknown element: " + type);
		}
	}
	
	private static String trimLines(String s) {
		if (s == null) return null;
		String[] lines = s.trim().split("[\r\n]");
		StringBuffer sb = new StringBuffer();
		for (String line : lines) {
			line = line.trim();
			if (line.length() > 0) {
				sb.append(line);
				sb.append('\n');
			}
		}
		return sb.toString();
	}
	
	private static PropertySet parseProperties(NamedNodeMap attr, String key) {
		if (attr == null) return null;
		Node node = attr.getNamedItem(key);
		if (node == null) return null;
		String text = node.getTextContent();
		if (text == null) return null;
		return PropertySet.parse(text.trim());
	}
	
	private static Dimension parseDimension(NamedNodeMap attr, String key) {
		if (attr == null) return null;
		Node node = attr.getNamedItem(key);
		if (node == null) return null;
		String text = node.getTextContent();
		if (text == null) return null;
		return new DimensionParser(text.trim()).parse();
	}
	
	private static ImageSource.Encoding parseImageSourceEncoding(NamedNodeMap attr, String key) {
		if (attr == null) return ImageSource.Encoding.RAW;
		Node node = attr.getNamedItem(key);
		if (node == null) return ImageSource.Encoding.RAW;
		String text = node.getTextContent();
		if (text == null) return ImageSource.Encoding.RAW;
		return ImageSource.Encoding.valueOf(text.trim().toUpperCase());
	}
	
	private static int parseInt(NamedNodeMap attr, String key, int def) {
		if (attr == null) return def;
		Node node = attr.getNamedItem(key);
		if (node == null) return def;
		String text = node.getTextContent();
		if (text == null) return def;
		try { return Integer.parseInt(text.trim()); }
		catch (NumberFormatException nfe) { return def; }
	}
	
	private static int[] parseIntArray(NamedNodeMap attr, String key, int def) {
		if (attr == null) return null;
		Node node = attr.getNamedItem(key);
		if (node == null) return null;
		String text = node.getTextContent();
		if (text == null) return null;
		String[] strings = text.trim().split("(\\s|,)+");
		int[] ints = new int[strings.length];
		for (int i = 0; i < strings.length; i++) {
			try { ints[i] = Integer.parseInt(strings[i]); }
			catch (NumberFormatException nfe) { ints[i] = def; }
		}
		return ints;
	}
	
	private static String parseString(NamedNodeMap attr, String key) {
		if (attr == null) return null;
		Node node = attr.getNamedItem(key);
		if (node == null) return null;
		String text = node.getTextContent();
		if (text == null) return null;
		return text.trim();
	}
	
	private static String[] parseStringArray(NamedNodeMap attr, String key) {
		if (attr == null) return null;
		Node node = attr.getNamedItem(key);
		if (node == null) return null;
		String text = node.getTextContent();
		if (text == null) return null;
		return text.trim().split("(\\s|,)+");
	}
	
	private static List<Node> getChildren(Node node) {
		List<Node> list = new ArrayList<Node>();
		if (node != null) {
			NodeList children = node.getChildNodes();
			if (children != null) {
				int count = children.getLength();
				for (int i = 0; i < count; i++) {
					Node child = children.item(i);
					if (child != null) {
						String type = child.getNodeName();
						if (type.equalsIgnoreCase("#text") || type.equalsIgnoreCase("#comment")) {
							continue;
						} else {
							list.add(child);
						}
					}
				}
			}
		}
		return list;
	}
	
	private static class KVXXEntityResolver implements EntityResolver {
		@Override
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if (publicId.contains("Vexillo") || systemId.contains("kvxx.dtd")) {
				return new InputSource(FlagParser.class.getResourceAsStream("kvxx.dtd"));
			} else {
				return null;
			}
		}
	}
	
	private static class KVXXErrorHandler implements ErrorHandler {
		private final String name;
		public KVXXErrorHandler(String name) {
			this.name = name;
		}
		@Override
		public void error(SAXParseException e) throws SAXException {
			System.err.print("Warning: Failed to compile flag " + name + ": ");
			System.err.println("ERROR on "+e.getLineNumber()+":"+e.getColumnNumber()+": "+e.getMessage());
		}
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			System.err.print("Warning: Failed to compile flag " + name + ": ");
			System.err.println("FATAL ERROR on "+e.getLineNumber()+":"+e.getColumnNumber()+": "+e.getMessage());
		}
		@Override
		public void warning(SAXParseException e) throws SAXException {
			System.err.print("Warning: Failed to compile flag " + name + ": ");
			System.err.println("WARNING on "+e.getLineNumber()+":"+e.getColumnNumber()+": "+e.getMessage());
		}
	}
}