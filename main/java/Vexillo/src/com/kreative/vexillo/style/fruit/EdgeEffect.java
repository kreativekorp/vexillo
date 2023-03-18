package com.kreative.vexillo.style.fruit;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.PathIterator;

public abstract class EdgeEffect {
	protected float minRiseOrRun;
	protected float epsilon;
	
	protected EdgeEffect(float minRiseOrRun, float epsilon) {
		this.minRiseOrRun = minRiseOrRun;
		this.epsilon = epsilon;
	}
	
	public final void apply(Graphics2D g, Shape s) {
		float startX = Float.NaN, startY = Float.NaN;
		float currX = Float.NaN, currY = Float.NaN;
		float[] coords = new float[6];
		for (PathIterator it = s.getPathIterator(null); !it.isDone(); it.next()) {
			switch (it.currentSegment(coords)) {
				case PathIterator.SEG_MOVETO:
					startX = currX = coords[0];
					startY = currY = coords[1];
					break;
				case PathIterator.SEG_LINETO:
					apply(g, s, currX, currY, coords[0], coords[1]);
					currX = coords[0];
					currY = coords[1];
					break;
				case PathIterator.SEG_QUADTO:
					currX = coords[2];
					currY = coords[3];
					break;
				case PathIterator.SEG_CUBICTO:
					currX = coords[4];
					currY = coords[5];
					break;
				case PathIterator.SEG_CLOSE:
					apply(g, s, currX, currY, startX, startY);
					currX = startX;
					currY = startY;
					break;
			}
		}
	}
	
	public abstract void apply(Graphics2D g, Shape s, float x1, float y1, float x2, float y2);
	
	protected final boolean isTopEdge(Shape s, float x1, float y1, float x2, float y2) {
		if (Math.abs(x1 - x2) <= minRiseOrRun) return false;
		float x = (x1 + x2) / 2, y = (y1 + y2) / 2;
		return s.contains(x, y + epsilon) && !s.contains(x, y - epsilon);
	}
	
	protected final boolean isBottomEdge(Shape s, float x1, float y1, float x2, float y2) {
		if (Math.abs(x1 - x2) <= minRiseOrRun) return false;
		float x = (x1 + x2) / 2, y = (y1 + y2) / 2;
		return s.contains(x, y - epsilon) && !s.contains(x, y + epsilon);
	}
	
	protected final boolean isLeftEdge(Shape s, float x1, float y1, float x2, float y2) {
		if (Math.abs(y1 - y2) <= minRiseOrRun) return false;
		float x = (x1 + x2) / 2, y = (y1 + y2) / 2;
		return s.contains(x + epsilon, y) && !s.contains(x - epsilon, y);
	}
	
	protected final boolean isRightEdge(Shape s, float x1, float y1, float x2, float y2) {
		if (Math.abs(y1 - y2) <= minRiseOrRun) return false;
		float x = (x1 + x2) / 2, y = (y1 + y2) / 2;
		return s.contains(x - epsilon, y) && !s.contains(x + epsilon, y);
	}
	
	protected final boolean isClockwise(Shape s, float x1, float y1, float x2, float y2) {
		double h = epsilon / Math.hypot(y2 - y1, x2 - x1);
		double x = (x1 + x2) / 2 - (y2 - y1) * h;
		double y = (y1 + y2) / 2 + (x2 - x1) * h;
		return s.contains(x, y);
	}
	
	protected final boolean isCounterClockwise(Shape s, float x1, float y1, float x2, float y2) {
		double h = epsilon / Math.hypot(y2 - y1, x2 - x1);
		double x = (x1 + x2) / 2 + (y2 - y1) * h;
		double y = (y1 + y2) / 2 - (x2 - x1) * h;
		return s.contains(x, y);
	}
}