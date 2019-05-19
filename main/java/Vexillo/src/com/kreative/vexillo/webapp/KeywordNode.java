package com.kreative.vexillo.webapp;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class KeywordNode implements Comparable<KeywordNode> {
	private final String[] ids;
	private final String[] nids;
	private final String[] keywords;
	
	public KeywordNode(String ids, String keywords) {
		this(ids.split("(\\s|;)+"), keywords.split("(\\s|;)+"));
	}
	
	public KeywordNode(String[] ids, String[] keywords) {
		SortedSet<String> idSet = new TreeSet<String>();
		SortedSet<String> nidSet = new TreeSet<String>();
		for (String id : ids) {
			String nid = id.toLowerCase().replaceAll("[^a-z0-9]+", "");
			if (id.length() > 0 && nid.length() > 0) {
				if (idSet.contains(id)) throw new IllegalArgumentException("Duplicate id: " + id);
				if (nidSet.contains(nid)) throw new IllegalArgumentException("Duplicate id:" + nid);
				idSet.add(id);
				nidSet.add(nid);
			}
		}
		if (idSet.isEmpty() || nidSet.isEmpty()) throw new IllegalArgumentException("No ids");
		this.ids = idSet.toArray(new String[idSet.size()]);
		this.nids = nidSet.toArray(new String[nidSet.size()]);
		this.keywords = new String[keywords.length];
		for (int i = 0; i < keywords.length; i++) {
			this.keywords[i] = keywords[i].toLowerCase().replaceAll("[^a-z0-9]+", "");
		}
	}
	
	public int countIds() {
		return this.ids.length;
	}
	
	public int countNormalizedIds() {
		return this.nids.length;
	}
	
	public int countKeywords() {
		return this.keywords.length;
	}
	
	public String getId(int i) {
		return this.ids[i];
	}
	
	public String getNormalizedId(int i) {
		return this.nids[i];
	}
	
	public String getKeyword(int i) {
		return this.keywords[i];
	}
	
	public String[] toIdArray() {
		String[] ids = new String[this.ids.length];
		for (int i = 0; i < this.ids.length; i++) {
			ids[i] = this.ids[i];
		}
		return ids;
	}
	
	public String[] toNormalizedIdArray() {
		String[] nids = new String[this.nids.length];
		for (int i = 0; i < this.nids.length; i++) {
			nids[i] = this.nids[i];
		}
		return nids;
	}
	
	public String[] toKeywordArray() {
		String[] keywords = new String[this.keywords.length];
		for (int i = 0; i < this.keywords.length; i++) {
			keywords[i] = this.keywords[i];
		}
		return keywords;
	}
	
	@Override
	public String toString() {
		return toString("\t");
	}
	
	public String toString(String d) {
		StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (int i = 0; i < ids.length; i++) {
			if (first) first = false;
			else sb.append("; ");
			sb.append(ids[i]);
		}
		sb.append(d);
		first = true;
		for (int i = 0; i < keywords.length; i++) {
			if (first) first = false;
			else sb.append(" ");
			sb.append(keywords[i]);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof KeywordNode) {
			KeywordNode that = (KeywordNode)o;
			return Arrays.equals(this.ids, that.ids)
			    && Arrays.equals(this.nids, that.nids)
			    && Arrays.equals(this.keywords, that.keywords);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.ids)
		     ^ Arrays.hashCode(this.nids)
		     ^ Arrays.hashCode(this.keywords);
	}
	
	@Override
	public int compareTo(KeywordNode that) {
		for (int i = 0; i < this.ids.length && i < that.ids.length; i++) {
			int cmp = this.ids[i].compareToIgnoreCase(that.ids[i]);
			if (cmp != 0) return cmp;
		}
		return this.ids.length - that.ids.length;
	}
}