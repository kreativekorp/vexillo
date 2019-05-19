package com.kreative.vexillo.webapp;

import java.util.Collections;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class NameSet {
	private final SortedSet<NameNode> nodes;
	private final SortedMap<String, NameNode> idMap;
	private final SortedMap<String, NameNode> nidMap;
	
	public NameSet() {
		this.nodes = new TreeSet<NameNode>();
		this.idMap = new TreeMap<String, NameNode>();
		this.nidMap = new TreeMap<String, NameNode>();
	}
	
	public NameSet parse(String[] lines) {
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
	
	public NameSet parse(Scanner scanner) {
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
				addNode(new NameNode(pieces[0], pieces[1]));
			} else {
				throw new IllegalArgumentException("Invalid field count");
			}
		}
	}
	
	public void addNode(NameNode node) {
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
	}
	
	public SortedSet<NameNode> getNodes() {
		return Collections.unmodifiableSortedSet(nodes);
	}
	
	public SortedMap<String, NameNode> getIdMap() {
		return Collections.unmodifiableSortedMap(idMap);
	}
	
	public SortedMap<String, NameNode> getNormalizedIdMap() {
		return Collections.unmodifiableSortedMap(nidMap);
	}
}