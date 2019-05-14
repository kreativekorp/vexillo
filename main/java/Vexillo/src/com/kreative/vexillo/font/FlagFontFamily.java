package com.kreative.vexillo.font;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.style.Stylizer;

public class FlagFontFamily {
	private final Encoding encoding;
	private final SortedSet<EncodingNode> nodes;
	private final SortedMap<EncodingNode, Flag> flags;
	private final SortedMap<EncodingNode, File> flagFiles;
	
	/* Font properties */
	public String name = "Kreative Vexillo";
	public String copyright = (
		"Copyright 2017-" +
		new GregorianCalendar().get(Calendar.YEAR) +
		" Kreative Software"
	);
	public String vendorId = "KrKo";
	public int emAscent = 900;
	public int emDescent = 300;
	public int lineAscent = 1200;
	public int lineDescent = 400;
	
	/* Glyph properties */
	public int spaceWidth = 400;
	public int leftBearing = 0;
	public int rightBearing = 0;
	public int glyphBottom = -100;
	public int glyphHeight = 1100;
	public int glyphWidth = 1600;
	public int bitmapHeight = 88;
	public int bitmapWidth = 128;
	public int bitmapGlaze = 8;
	public Stylizer bitmapStyle = null;
	
	public FlagFontFamily(Encoding encoding) {
		this.encoding = encoding;
		this.nodes = new TreeSet<EncodingNode>();
		this.flags = new TreeMap<EncodingNode, Flag>();
		this.flagFiles = new TreeMap<EncodingNode, File>();
	}
	
	public boolean addFlag(String id, Flag flag, File flagFile) {
		String nid = id.toLowerCase().replaceAll("[^a-z0-9]+", "");
		if (nid.length() == 0) return false;
		EncodingNode node = encoding.getNormalizedIdMap().get(nid);
		if (node == null) return false;
		nodes.add(node);
		flags.put(node, flag);
		flagFiles.put(node, flagFile);
		return true;
	}
	
	public SortedSet<EncodingNode> getNodes() {
		return Collections.unmodifiableSortedSet(nodes);
	}
	
	public Flag getFlag(EncodingNode node) {
		return flags.get(node);
	}
	
	public SortedMap<EncodingNode, Flag> getFlagMap() {
		return Collections.unmodifiableSortedMap(flags);
	}
	
	public File getFlagFile(EncodingNode node) {
		return flagFiles.get(node);
	}
	
	public SortedMap<EncodingNode, File> getFlagFileMap() {
		return Collections.unmodifiableSortedMap(flagFiles);
	}
	
	public int emUnitsToPixels(float u) {
		return Math.round(u * bitmapHeight / glyphHeight);
	}
	
	public int getPixelsPerEm() {
		return emUnitsToPixels(emAscent + emDescent);
	}
}