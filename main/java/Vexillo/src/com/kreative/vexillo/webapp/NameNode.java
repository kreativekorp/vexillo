package com.kreative.vexillo.webapp;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class NameNode implements Comparable<NameNode> {
	private final String[] ids;
	private final String[] nids;
	private final String name;
	
	public NameNode(String ids, String name) {
		this(ids.split("(\\s|;)+"), name);
	}
	
	public NameNode(String[] ids, String name) {
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
		this.name = name.toUpperCase().replaceAll("[^A-Z0-9 -]+", "");
	}
	
	public int countIds() {
		return this.ids.length;
	}
	
	public int countNormalizedIds() {
		return this.nids.length;
	}
	
	public String getId(int i) {
		return this.ids[i];
	}
	
	public String getNormalizedId(int i) {
		return this.nids[i];
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
	
	public String getName() {
		return this.name;
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
		sb.append(name);
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof NameNode) {
			NameNode that = (NameNode)o;
			return Arrays.equals(this.ids, that.ids)
			    && Arrays.equals(this.nids, that.nids)
			    && this.name.equals(that.name);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.ids)
		     ^ Arrays.hashCode(this.nids)
		     ^ this.name.hashCode();
	}
	
	@Override
	public int compareTo(NameNode that) {
		for (int i = 0; i < this.ids.length && i < that.ids.length; i++) {
			int cmp = this.ids[i].compareToIgnoreCase(that.ids[i]);
			if (cmp != 0) return cmp;
		}
		return this.ids.length - that.ids.length;
	}
}