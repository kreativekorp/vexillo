package com.kreative.vexillo.font;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public class EncodingNode implements Comparable<EncodingNode> {
	private final String[] ids;
	private final String[] nids;
	private final CodeSequence[] seqs;
	
	public EncodingNode(String[] ids, CodeSequence[] seqs) {
		SortedSet<String> idSet = new TreeSet<String>();
		SortedSet<String> nidSet = new TreeSet<String>();
		for (int i = 0; i < ids.length; i++) {
			String nid = ids[i].toLowerCase().replaceAll("[^a-z0-9]+", "");
			if (ids[i].length() > 0 && nid.length() > 0) {
				if (idSet.contains(ids[i])) throw new IllegalArgumentException("Duplicate id: " + ids[i]);
				if (nidSet.contains(nid)) throw new IllegalArgumentException("Duplicate id:" + nid);
				idSet.add(ids[i]);
				nidSet.add(nid);
			}
		}
		if (idSet.isEmpty() || nidSet.isEmpty()) throw new IllegalArgumentException("No ids");
		this.ids = idSet.toArray(new String[idSet.size()]);
		this.nids = nidSet.toArray(new String[nidSet.size()]);
		SortedSet<CodeSequence> seqSet = new TreeSet<CodeSequence>();
		for (int i = 0; i < seqs.length; i++) {
			if (seqs[i].length() > 0) {
				if (seqSet.contains(seqs[i])) throw new IllegalArgumentException("Duplicate code sequence: " + seqs[i]);
				seqSet.add(seqs[i]);
			}
		}
		if (seqSet.isEmpty()) throw new IllegalArgumentException("No code sequences");
		this.seqs = seqSet.toArray(new CodeSequence[seqSet.size()]);
	}
	
	public EncodingNode(String ids, String seqs) {
		SortedSet<String> idSet = new TreeSet<String>();
		SortedSet<String> nidSet = new TreeSet<String>();
		String[] idsArray = ids.split("(\\s|;)+");
		for (String id : idsArray) {
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
		SortedSet<CodeSequence> seqSet = new TreeSet<CodeSequence>();
		String[] seqsArray = seqs.split("(\\s|;)+");
		for (String seqString : seqsArray) {
			CodeSequence seq = new CodeSequence(seqString);
			if (seq.length() > 0) {
				if (seqSet.contains(seq)) throw new IllegalArgumentException("Duplicate code sequence: " + seq);
				seqSet.add(seq);
			}
		}
		if (seqSet.isEmpty()) throw new IllegalArgumentException("No code sequences");
		this.seqs = seqSet.toArray(new CodeSequence[seqSet.size()]);
	}
	
	public int countIds() {
		return this.ids.length;
	}
	
	public int countNormalizedIds() {
		return this.nids.length;
	}
	
	public int countCodeSequences() {
		return this.seqs.length;
	}
	
	public String getId(int i) {
		return this.ids[i];
	}
	
	public String getNormalizedId(int i) {
		return this.nids[i];
	}
	
	public CodeSequence getCodeSequence(int i) {
		return this.seqs[i];
	}
	
	public CodeSequence getCanonicalCodeSequence() {
		for (CodeSequence seq : seqs) {
			if (seq.length() == 1 && isPUA(seq.codePointAt(0))) {
				return seq;
			}
		}
		for (CodeSequence seq : seqs) {
			if (seq.length() == 1) {
				return seq;
			}
		}
		return seqs[0];
	}
	
	private boolean isPUA(int cp) {
		return (
			(cp >= 0xE000 && cp < 0xF900) ||
			(cp >= 0xF0000 && cp < 0xFFFFE) ||
			(cp >= 0x100000 && cp < 0x10FFFE)
		);
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
	
	public CodeSequence[] toCodeSequenceArray() {
		CodeSequence[] seqs = new CodeSequence[this.seqs.length];
		for (int i = 0; i < this.seqs.length; i++) {
			seqs[i] = this.seqs[i];
		}
		return seqs;
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
		for (int i = 0; i < seqs.length; i++) {
			if (first) first = false;
			else sb.append("; ");
			sb.append(seqs[i]);
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof EncodingNode) {
			EncodingNode that = (EncodingNode)o;
			return Arrays.equals(this.ids, that.ids)
			    && Arrays.equals(this.nids, that.nids)
			    && Arrays.equals(this.seqs, that.seqs);
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.ids)
		     ^ Arrays.hashCode(this.nids)
		     ^ Arrays.hashCode(this.seqs);
	}
	
	@Override
	public int compareTo(EncodingNode that) {
		CodeSequence ccs = this.getCanonicalCodeSequence();
		return ccs.compareTo(that.getCanonicalCodeSequence());
	}
}