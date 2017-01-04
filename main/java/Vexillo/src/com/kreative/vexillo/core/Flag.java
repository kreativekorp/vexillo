package com.kreative.vexillo.core;

import java.util.ArrayList;
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
	
	public Map<String, Dimension> dimensions() { return this.dimensions; }
	public Map<String, Color> colors() { return this.colors; }
	public Map<String, Symbol> symbols() { return this.symbols; }
	public Map<String, Image> images() { return this.images; }
	public List<Instruction> instructions() { return this.instructions; }
}