package com.kreative.vexillo.style.fruit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class EdgeEffectTest {
	public static void main(String[] args) {
		final List<Integer> x = new ArrayList<Integer>();
		final List<Integer> y = new ArrayList<Integer>();
		final int[] n = new int[1];
		
		final JComponent view = new JComponent() {
			private static final long serialVersionUID = 1L;
			protected void paintComponent(Graphics g) {
				Polygon p = makePolygon(x, y, n[0]);
				if (p != null) {
					g.setColor(Color.white);
					((Graphics2D)g).fill(p);
					((Graphics2D)g).setStroke(new BasicStroke(8));
					effect.apply((Graphics2D)g, p);
				}
				g.setColor(Color.green);
				for (int i = 0; i < n[0]; i++) {
					g.fillOval((int)x.get(i)-8, (int)y.get(i)-8, 16, 16);
				}
			}
		};
		view.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1) {
					x.clear();
					y.clear();
					n[0] = 0;
				}
				if (e.isShiftDown() && n[0] > 0) {
					int lastX = x.get(n[0]-1);
					int lastY = y.get(n[0]-1);
					if (Math.abs(lastY - e.getY()) > Math.abs(lastX - e.getX())) {
						x.add(lastX);
						y.add(e.getY());
					} else {
						x.add(e.getX());
						y.add(lastY);
					}
				} else {
					x.add(e.getX());
					y.add(e.getY());
				}
				n[0]++;
				view.repaint();
			}
		});
		
		final JFrame frame = new JFrame("EdgeEffectTest");
		frame.setContentPane(view);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private static Polygon makePolygon(List<Integer> x, List<Integer> y, int n) {
		if (n < 2) return null;
		int[] xa = new int[n];
		int[] ya = new int[n];
		for (int i = 0; i < n; i++) {
			xa[i] = x.get(i);
			ya[i] = y.get(i);
		}
		return new Polygon(xa, ya, n);
	}
	
	private static final EdgeEffect effect = new EdgeEffect(10, 1) {
		private final Line2D.Float line = new Line2D.Float();
		public void apply(Graphics2D g, Shape s, float x1, float y1, float x2, float y2) {
			g.setColor(palette[
				(isLeftEdge(s, x1, y1, x2, y2) ? 0 : isRightEdge(s, x1, y1, x2, y2) ? 2 : 1) +
				(isTopEdge(s, x1, y1, x2, y2) ? 0 : isBottomEdge(s, x1, y1, x2, y2) ? 6 : 3)
			]);
			
			line.setLine(x1, y1, x2, y2);
			g.draw(line);
			
			g.draw(align(
				testShape, x1, y1, x2, y2,
				isClockwise(s, x1, y1, x2, y2)
			));
			// positive Y is up: x1 < x2
			// positive Y is down: x1 > x2
			// positive Y is left: y1 > y2
			// positive Y is right: y1 < y2
			// positive Y is outside s: isClockwise(s, x1, y1, x2, y2)
			// positive Y is inside s: isCounterClockwise(s, x1, y1, x2, y2)
			
			g.setColor(
				isClockwise(s, x1, y1, x2, y2) ? Color.red :
				isCounterClockwise(s, x1, y1, x2, y2) ? Color.blue :
				Color.gray
			);
			
			g.fillOval(
				(int)((x1 + x2) / 2 - 8),
				(int)((y1 + y2) / 2 - 8),
				16, 16
			);
		}
	};
	
	private static final Color[] palette = new Color[] {
		new Color(0xDD0000), new Color(0xFF8800), new Color(0xFFEE00),
		new Color(0xFF00DD), new Color(0x808080), new Color(0x00CC00),
		new Color(0x9900FF), new Color(0x0000DD), new Color(0x00CCFF),
	};
	
	private static final Shape testShape = new QuadCurve2D.Double(0, 0, 0.5, 0.25, 1, 0);
	
	private static Shape align(Shape s, float x1, float y1, float x2, float y2, boolean swap) {
		if (swap) return align(s, x2, y2, x1, y1, false);
		double scale = Math.hypot(y2 - y1, x2 - x1);
		double angle = Math.atan2(y2 - y1, x2 - x1);
		AffineTransform tx = new AffineTransform();
		tx.translate(x1, y1);
		tx.scale(scale, scale);
		tx.rotate(angle);
		return tx.createTransformedShape(s);
	}
}