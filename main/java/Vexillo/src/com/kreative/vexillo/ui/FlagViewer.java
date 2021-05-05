package com.kreative.vexillo.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.JComponent;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagRenderer;

public class FlagViewer extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private static final int MINIMUM_WIDTH = 16;
	private static final int MINIMUM_HEIGHT = 10;
	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 200;
	
	private File parent;
	private Flag flag;
	private FlagRenderer renderer;
	private boolean glaze;
	private int glazeAmount;
	private Dimension minSize;
	private Dimension prefSize;
	
	public FlagViewer(File parent, Flag flag) {
		this.parent = parent;
		this.flag = flag;
		this.renderer = new FlagRenderer(parent, flag);
		this.glaze = false;
		this.glazeAmount = 8;
		this.minSize = null;
		this.prefSize = null;
	}
	
	public File getParentFile() {
		return parent;
	}
	
	public Flag getFlag() {
		return flag;
	}
	
	public void setFlag(File parent, Flag flag) {
		this.parent = parent;
		this.flag = flag;
		this.renderer = new FlagRenderer(parent, flag);
		this.repaint();
	}
	
	public boolean isGlazed() {
		return glaze;
	}
	
	public int getGlaze() {
		return glaze ? glazeAmount : 0;
	}
	
	public int getGlazeAmount() {
		return glazeAmount;
	}
	
	public void setGlaze(boolean glaze) {
		this.glaze = glaze;
		this.repaint();
	}
	
	public void setGlaze(boolean glaze, int glazeAmount) {
		this.glaze = glaze;
		this.glazeAmount = (glazeAmount > 1) ? glazeAmount : 1;
		this.repaint();
	}
	
	public void setGlaze(int glazeAmount) {
		this.glaze = (glazeAmount > 0);
		if (this.glaze) this.glazeAmount = glazeAmount;
		this.repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		if (flag != null) {
			Insets i = getInsets();
			int w = getWidth() - i.left - i.right;
			int h = getHeight() - i.top - i.bottom;
			int gl = glaze ? glazeAmount : 0;
			BufferedImage img = renderer.renderToImage(w, h, null, 0, gl);
			g.drawImage(img, i.left, i.top, null);
		}
	}
	
	@Override
	public void setMinimumSize(Dimension minSize) {
		this.minSize = minSize;
	}
	
	@Override
	public Dimension getMinimumSize() {
		if (this.minSize != null) {
			return this.minSize;
		} else {
			Insets i = getInsets();
			return new Dimension(
				i.left + i.right + MINIMUM_WIDTH,
				i.top + i.bottom + MINIMUM_HEIGHT
			);
		}
	}
	
	@Override
	public void setPreferredSize(Dimension prefSize) {
		this.prefSize = prefSize;
	}
	
	@Override
	public Dimension getPreferredSize() {
		if (this.prefSize != null) {
			return this.prefSize;
		} else if (flag != null) {
			Insets i = getInsets();
			int w = flag.getWidthFromHeight(DEFAULT_HEIGHT);
			return new Dimension(
				i.left + i.right + w,
				i.top + i.bottom + DEFAULT_HEIGHT
			);
		} else {
			Insets i = getInsets();
			return new Dimension(
				i.left + i.right + DEFAULT_WIDTH,
				i.top + i.bottom + DEFAULT_HEIGHT
			);
		}
	}
}