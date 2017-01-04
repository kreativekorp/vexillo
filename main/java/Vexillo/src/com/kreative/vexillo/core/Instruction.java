package com.kreative.vexillo.core;

import java.util.ArrayList;
import java.util.List;

public abstract class Instruction {
	public static final class GroupInstruction extends Instruction {
		public final List<Instruction> clippingRegion;
		public final List<Instruction> instructions;
		public GroupInstruction() {
			this.clippingRegion = new ArrayList<Instruction>();
			this.instructions = new ArrayList<Instruction>();
		}
	}
	
	public static final class ForInstruction extends Instruction {
		public final String var;
		public final Dimension start, end, step;
		public final List<Instruction> instructions;
		public ForInstruction(
			String var, Dimension start, Dimension end, Dimension step
		) {
			this.var = var;
			this.start = start; this.end = end; this.step = step;
			this.instructions = new ArrayList<Instruction>();
		}
	}
	
	public static final class FieldInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final String color;
		public FieldInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			String color
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.color = color;
		}
	}
	
	public static final class HBandInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final int bands;
		public final List<String> bandColors;
		public final List<Integer> bandWeights;
		public HBandInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2, int bands
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.bands = bands;
			this.bandColors = new ArrayList<String>();
			this.bandWeights = new ArrayList<Integer>();
		}
		public String getBandColor(int band) {
			if (bandColors.isEmpty()) return null;
			else return bandColors.get(band % bandColors.size());
		}
		public int getBandWeight(int band) {
			if (bandWeights.isEmpty()) return 1;
			else return bandWeights.get(band % bandWeights.size());
		}
		public int getBandWeightTotal() {
			int total = 0;
			for (int i = 0; i < bands; i++) total += getBandWeight(i);
			return total;
		}
	}
	
	public static final class VBandInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final int bands;
		public final List<String> bandColors;
		public final List<Integer> bandWeights;
		public VBandInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2, int bands
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.bands = bands;
			this.bandColors = new ArrayList<String>();
			this.bandWeights = new ArrayList<Integer>();
		}
		public String getBandColor(int band) {
			if (bandColors.isEmpty()) return null;
			else return bandColors.get(band % bandColors.size());
		}
		public int getBandWeight(int band) {
			if (bandWeights.isEmpty()) return 1;
			else return bandWeights.get(band % bandWeights.size());
		}
		public int getBandWeightTotal() {
			int total = 0;
			for (int i = 0; i < bands; i++) total += getBandWeight(i);
			return total;
		}
	}
	
	public static final class CrossInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final Dimension x3, y3, x4, y4;
		public final String color;
		public CrossInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			Dimension x3, Dimension y3, Dimension x4, Dimension y4,
			String color
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.x3 = x3; this.y3 = y3; this.x4 = x4; this.y4 = y4;
			this.color = color;
		}
	}
	
	public static final class SaltireInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final Dimension thickness;
		public final String color;
		public SaltireInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			Dimension thickness, String color
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.thickness = thickness; this.color = color;
		}
	}
	
	public static final class DBandInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final Dimension thickness;
		public final String color;
		public DBandInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			Dimension thickness, String color
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.thickness = thickness; this.color = color;
		}
	}
	
	public static final class DiscInstruction extends Instruction {
		public final Dimension centerX, centerY, width, height;
		public final Dimension startAngle, endAngle;
		public final String color;
		public DiscInstruction(
			Dimension centerX, Dimension centerY,
			Dimension width, Dimension height,
			Dimension startAngle, Dimension endAngle,
			String color
		) {
			this.centerX = centerX; this.centerY = centerY;
			this.width = width; this.height = height;
			this.startAngle = startAngle; this.endAngle = endAngle;
			this.color = color;
		}
	}
	
	public static final class PolyInstruction extends Instruction {
		public final int points;
		public final Dimension[] x, y;
		public final String color;
		public PolyInstruction(
			int points, Dimension[] x, Dimension[] y, String color
		) {
			this.points = points;
			this.x = new Dimension[points];
			this.y = new Dimension[points];
			for (int i = 0; i < points; i++) {
				this.x[i] = x[i % x.length];
				this.y[i] = y[i % y.length];
			}
			this.color = color;
		}
	}
	
	public static final class SymbolInstruction extends Instruction {
		public final String symbolName;
		public final Dimension x, y, sx, sy, rotate;
		public final String color;
		public SymbolInstruction(
			String symbolName, Dimension x, Dimension y,
			Dimension sx, Dimension sy, Dimension rotate,
			String color
		) {
			this.symbolName = symbolName; this.x = x; this.y = y;
			this.sx = sx; this.sy = sy; this.rotate = rotate;
			this.color = color;
		}
	}
	
	public static final class ImageInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final String imageName;
		public ImageInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			String imageName
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.imageName = imageName;
		}
	}
	
	public static final class HGradInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final String color1, color2;
		public HGradInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			String color1, String color2
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.color1 = color1; this.color2 = color2;
		}
	}
	
	public static final class VGradInstruction extends Instruction {
		public final Dimension x1, y1, x2, y2;
		public final String color1, color2;
		public VGradInstruction(
			Dimension x1, Dimension y1, Dimension x2, Dimension y2,
			String color1, String color2
		) {
			this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2;
			this.color1 = color1; this.color2 = color2;
		}
	}
}