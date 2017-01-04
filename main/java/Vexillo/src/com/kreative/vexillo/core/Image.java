package com.kreative.vexillo.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Image implements List<ImageSource> {
	private final List<ImageSource> list = new ArrayList<ImageSource>();
	@Override public boolean add(ImageSource e) { return list.add(e); }
	@Override public void add(int i, ImageSource e) { list.add(i, e); }
	@Override public boolean addAll(Collection<? extends ImageSource> c) { return list.addAll(c); }
	@Override public boolean addAll(int i, Collection<? extends ImageSource> c) { return list.addAll(i, c); }
	@Override public void clear() { list.clear(); }
	@Override public boolean contains(Object o) { return list.contains(o); }
	@Override public boolean containsAll(Collection<?> c) { return list.containsAll(c); }
	@Override public ImageSource get(int i) { return list.get(i); }
	@Override public int indexOf(Object o) { return list.indexOf(o); }
	@Override public boolean isEmpty() { return list.isEmpty(); }
	@Override public Iterator<ImageSource> iterator() { return list.iterator(); }
	@Override public int lastIndexOf(Object o) { return list.lastIndexOf(o); }
	@Override public ListIterator<ImageSource> listIterator() { return list.listIterator(); }
	@Override public ListIterator<ImageSource> listIterator(int i) { return list.listIterator(i); }
	@Override public boolean remove(Object o) { return list.remove(o); }
	@Override public ImageSource remove(int i) { return list.remove(i); }
	@Override public boolean removeAll(Collection<?> c) { return list.removeAll(c); }
	@Override public boolean retainAll(Collection<?> c) { return list.retainAll(c); }
	@Override public ImageSource set(int i, ImageSource e) { return list.set(i, e); }
	@Override public int size() { return list.size(); }
	@Override public List<ImageSource> subList(int i, int j) { return list.subList(i, j); }
	@Override public Object[] toArray() { return list.toArray(); }
	@Override public <T> T[] toArray(T[] a) { return list.toArray(a); }
}