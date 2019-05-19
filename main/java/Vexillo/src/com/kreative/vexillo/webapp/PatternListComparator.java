package com.kreative.vexillo.webapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public abstract class PatternListComparator<T> implements Comparator<T> {
	private static final class Entry {
		public final Pattern pattern;
		public final int specificity;
		public Entry(String pattern, int specificity) {
			this.pattern = Pattern.compile(pattern);
			this.specificity = specificity;
		}
	}
	
	private final List<Entry> entries = new ArrayList<Entry>();
	private final Map<String,Integer> cache = new HashMap<String,Integer>();
	
	public final void add(String pattern, int specificity) {
		entries.add(new Entry(pattern, specificity));
		cache.clear();
	}
	public final void add(int index, String pattern, int specificity) {
		entries.add(index, new Entry(pattern, specificity));
		cache.clear();
	}
	public final void clear() {
		entries.clear();
		cache.clear();
	}
	public final Pattern getPattern(int index) {
		return entries.get(index).pattern;
	}
	public final int getSpecificity(int index) {
		return entries.get(index).specificity;
	}
	public final boolean isEmpty() {
		return entries.isEmpty();
	}
	public final void remove(int index) {
		entries.remove(index);
		cache.clear();
	}
	public final void set(int index, String pattern, int specificity) {
		entries.set(index, new Entry(pattern, specificity));
		cache.clear();
	}
	public final int size() {
		return entries.size();
	}
	
	public final void parse(Scanner scan) {
		while (scan.hasNextLine()) {
			String line = scan.nextLine().trim();
			if (line.length() > 0 && !line.startsWith("#")) {
				String[] fields = line.split("\\s+", 2);
				if (fields.length > 1) {
					try { add(fields[1], Integer.parseInt(fields[0])); }
					catch (NumberFormatException nfe) {}
				}
			}
		}
	}
	
	private final int getSortOrder(String key) {
		if (cache.containsKey(key)) return cache.get(key);
		int currentSpecificity = Integer.MIN_VALUE;
		int currentSortOrder = entries.size();
		for (int i = 0, n = entries.size(); i < n; i++) {
			Entry e = entries.get(i);
			if (
				e.specificity > currentSpecificity &&
				e.pattern.matcher(key).matches()
			) {
				currentSpecificity = e.specificity;
				currentSortOrder = i;
			}
		}
		cache.put(key, currentSortOrder);
		return currentSortOrder;
	}
	
	public abstract String getKey(T t);
	
	@Override
	public final int compare(T a, T b) {
		String ak = getKey(a), bk = getKey(b);
		int ao = getSortOrder(ak), bo = getSortOrder(bk);
		if (ao != bo) return ao - bo;
		return ak.compareToIgnoreCase(bk);
	}
	
	public static final class ForStrings extends PatternListComparator<String> {
		@Override
		public String getKey(String t) {
			return t;
		}
	}
}