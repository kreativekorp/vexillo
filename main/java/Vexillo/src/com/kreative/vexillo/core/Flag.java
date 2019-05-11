package com.kreative.vexillo.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Flag {
	private String id;
	private String name;
	private PropertySet properties;
	private Dimension fly;
	private Map<String, Dimension> dimensions;
	private Map<String, Color> colors;
	private Map<String, Symbol> symbols;
	private Map<String, Image> images;
	private List<Instruction> instructions;
	
	public Flag() {
		this.id = null;
		this.name = null;
		this.properties = new PropertySet();
		this.fly = new Dimension.Variable("h");
		this.dimensions = new TreeMap<String, Dimension>();
		this.colors = new TreeMap<String, Color>();
		this.symbols = new TreeMap<String, Symbol>();
		this.images = new TreeMap<String, Image>();
		this.instructions = new ArrayList<Instruction>();
	}
	
	public String getId() { return this.id; }
	public void setId(String id) { this.id = id; }
	
	public String getName() { return this.name; }
	public void setName(String name) { this.name = name; }
	
	public PropertySet getProperties() { return this.properties; }
	public void setProperties(PropertySet props) { this.properties = props; }
	
	public Dimension getFly() { return this.fly; }
	public void setFly(Dimension fly) { this.fly = fly; }
	
	public int getWidthFromHeight(int height) {
		return (int)Math.round(getWidthFromHeight2D(height));
	}
	
	public int getHeightFromWidth(int width) {
		return (int)Math.round(getHeightFromWidth2D(width));
	}
	
	public double getWidthFromHeight2D(double height) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.putAll(dimensions);
		return fly.value(ns);
	}
	
	public double getHeightFromWidth2D(double width) {
		return width / getWidthFromHeight2D(1);
	}
	
	public String getProportionString() {
		int bestDenom = 1;
		double bestNum = getWidthFromHeight2D(1);
		double bestError = Math.abs(bestNum - Math.round(bestNum));
		if (bestError == 0) return bestDenom + ":" + (int)Math.round(bestNum);
		for (int denom = 2; denom < 1000; denom++) {
			double num = getWidthFromHeight2D(denom);
			double error = Math.abs(num - Math.round(num));
			if (error == 0) return denom + ":" + (int)Math.round(num);
			else if (error < bestError) {
				bestDenom = denom;
				bestNum = num;
				bestError = error;
			}
		}
		return bestDenom + ":" + bestNum;
	}
	
	public Map<String, Dimension> dimensions() { return this.dimensions; }
	public Map<String, Color> colors() { return this.colors; }
	public Map<String, Symbol> symbols() { return this.symbols; }
	public Map<String, Image> images() { return this.images; }
	public List<Instruction> instructions() { return this.instructions; }
	
	public Map<String, Dimension> createNamespace(double height, double width) {
		Map<String, Dimension> ns = new HashMap<String, Dimension>();
		Dimension h = new Dimension.Constant(height);
		Dimension w = new Dimension.Constant(width);
		ns.put("h", h);
		ns.put("hoist", h);
		ns.put("height", h);
		ns.put("w", w);
		ns.put("width", w);
		ns.put("f", fly);
		ns.put("fly", fly);
		ns.putAll(dimensions);
		return ns;
	}
}