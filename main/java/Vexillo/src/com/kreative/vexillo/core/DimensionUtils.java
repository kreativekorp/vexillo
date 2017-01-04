package com.kreative.vexillo.core;

import java.util.HashMap;
import java.util.Map;

public class DimensionUtils {
	public static final Map<String, Dimension> createNamespace(
		Map<String, Dimension> namespace, double height
	) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.putAll(namespace);
		return ns;
	}
	
	public static final Map<String, Dimension> createNamespace(
		Map<String, Dimension> namespace, double height, double width
	) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		Dimension w = new Dimension.Constant(width);
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.put("w", w);
		ns.put("width", w);
		ns.putAll(namespace);
		return ns;
	}
	
	public static final Map<String, Dimension> createNamespace(
		Map<String, Dimension> namespace,
		double height, double width, double fly
	) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		Dimension w = new Dimension.Constant(width);
		Dimension f = new Dimension.Constant(fly);
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.put("w", w);
		ns.put("width", w);
		ns.put("f", f);
		ns.put("fly", f);
		ns.putAll(namespace);
		return ns;
	}
	
	public static final Map<String, Dimension> createNamespace(
		Map<String, Dimension> namespace,
		double height, double width, Dimension fly
	) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		Dimension w = new Dimension.Constant(width);
		Dimension f = fly;
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.put("w", w);
		ns.put("width", w);
		ns.put("f", f);
		ns.put("fly", f);
		ns.putAll(namespace);
		return ns;
	}
}