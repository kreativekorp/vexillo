package com.kreative.vexillo.core;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PropertySet implements Set<Property> {
	public static PropertySet parse(String s) {
		return parse(s.trim().split("\\s+"));
	}
	
	public static PropertySet parse(String[] ss) {
		PropertySet a = new PropertySet();
		for (String s : ss) a.add(Property.parse(s));
		return a;
	}
	
	private final EnumSet<Property> internal;
	
	public PropertySet() {
		internal = EnumSet.noneOf(Property.class);
	}
	
	public int[] getCodePoints() {
		Property[] values = Property.values();
		int[] codePoints = new int[values.length];
		int codePointCount = 0;
		Map<Integer,Integer> codePointGroupIndexMap = new HashMap<Integer,Integer>();
		for (Property value : values) {
			if (internal.contains(value)) {
				int codePoint = value.getCodePoint();
				int codePointGroup = value.getCodePointGroup();
				if (codePointGroup == 0) {
					codePoints[codePointCount++] = codePoint;
				} else if (codePointGroupIndexMap.containsKey(codePointGroup)) {
					int index = codePointGroupIndexMap.get(codePointGroup);
					codePoints[index] |= codePoint;
				} else {
					codePointGroupIndexMap.put(codePointGroup, codePointCount);
					codePoints[codePointCount++] = codePoint;
				}
			}
		}
		int[] ret = new int[codePointCount];
		for (int i = 0; i < codePointCount; i++) {
			ret[i] = codePoints[i];
		}
		return ret;
	}
	
	public String getCodePointString() {
		StringBuffer s = new StringBuffer();
		int[] codePoints = getCodePoints();
		for (int codePoint : codePoints) {
			s.append(Character.toChars(codePoint));
		}
		return s.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		boolean first = true;
		for (Property value : internal) {
			if (first) first = false;
			else s.append(" ");
			s.append(value.toString());
		}
		return s.toString();
	}
	
	@Override public boolean add(Property e) { return internal.add(e); }
	@Override public boolean addAll(Collection<? extends Property> c) { return internal.addAll(c); }
	@Override public void clear() { internal.clear(); }
	@Override public boolean contains(Object o) { return internal.contains(o); }
	@Override public boolean containsAll(Collection<?> c) { return internal.containsAll(c); }
	@Override public boolean isEmpty() { return internal.isEmpty(); }
	@Override public Iterator<Property> iterator() { return internal.iterator(); }
	@Override public boolean remove(Object o) { return internal.remove(o); }
	@Override public boolean removeAll(Collection<?> c) { return internal.removeAll(c); }
	@Override public boolean retainAll(Collection<?> c) { return internal.retainAll(c); }
	@Override public int size() { return internal.size(); }
	@Override public Object[] toArray() { return internal.toArray(); }
	@Override public <T> T[] toArray(T[] a) { return internal.toArray(a); }
}