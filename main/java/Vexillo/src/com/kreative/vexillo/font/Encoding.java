package com.kreative.vexillo.font;

import java.util.Collections;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Encoding {
	private final SortedSet<EncodingNode> nodes;
	private final SortedMap<String, EncodingNode> idMap;
	private final SortedMap<String, EncodingNode> nidMap;
	private final SortedMap<CodeSequence, EncodingNode> seqMap;
	
	public Encoding() {
		this.nodes = new TreeSet<EncodingNode>();
		this.idMap = new TreeMap<String, EncodingNode>();
		this.nidMap = new TreeMap<String, EncodingNode>();
		this.seqMap = new TreeMap<CodeSequence, EncodingNode>();
	}
	
	public Encoding parse(String[] lines) {
		int lineNum = 1;
		for (String line : lines) {
			try {
				parseLine(line);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Line " + lineNum + ": " + e.getMessage(), e);
			}
			lineNum++;
		}
		return this;
	}
	
	public Encoding parse(Scanner scanner) {
		int lineNum = 1;
		while (scanner.hasNextLine()) {
			try {
				parseLine(scanner.nextLine());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Line " + lineNum + ": " + e.getMessage(), e);
			}
			lineNum++;
		}
		return this;
	}
	
	private void parseLine(String line) {
		line = line.trim();
		if (line.length() > 0 && !line.startsWith("#")) {
			String[] pieces = line.split("\\s\\s+|\t");
			if (pieces.length == 2) {
				addNode(new EncodingNode(pieces[0], pieces[1]));
			} else {
				throw new IllegalArgumentException("Invalid field count");
			}
		}
	}
	
	public void addNode(EncodingNode node) {
		if (nodes.contains(node)) throw new IllegalArgumentException("Duplicate node: " + node.toString(" -> "));
		nodes.add(node);
		for (int i = 0; i < node.countIds(); i++) {
			String id = node.getId(i);
			if (idMap.containsKey(id)) throw new IllegalArgumentException("Duplicate id: " + id);
			idMap.put(id, node);
		}
		for (int i = 0; i < node.countNormalizedIds(); i++) {
			String nid = node.getNormalizedId(i);
			if (nidMap.containsKey(nid)) throw new IllegalArgumentException("Duplicate id: " + nid);
			nidMap.put(nid, node);
		}
		for (int i = 0; i < node.countCodeSequences(); i++) {
			CodeSequence seq = node.getCodeSequence(i);
			if (seqMap.containsKey(seq)) throw new IllegalArgumentException("Duplicate code sequence: " + seq);
			seqMap.put(seq, node);
		}
	}
	
	public SortedSet<EncodingNode> getNodes() {
		return Collections.unmodifiableSortedSet(nodes);
	}
	
	public SortedMap<String, EncodingNode> getIdMap() {
		return Collections.unmodifiableSortedMap(idMap);
	}
	
	public SortedMap<String, EncodingNode> getNormalizedIdMap() {
		return Collections.unmodifiableSortedMap(nidMap);
	}
	
	public SortedMap<CodeSequence, EncodingNode> getCodeSequenceMap() {
		return Collections.unmodifiableSortedMap(seqMap);
	}
}