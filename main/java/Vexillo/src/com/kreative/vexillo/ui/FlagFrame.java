package com.kreative.vexillo.ui;

import java.io.File;
import javax.swing.JFrame;
import com.kreative.vexillo.core.Flag;

public class FlagFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final FlagPanel panel;
	
	public FlagFrame(String title, File flagFile, File parent, Flag flag) {
		super(title);
		setJMenuBar(new FlagMenuBar(this));
		this.panel = new FlagPanel(flagFile, parent, flag);
		setContentPane(panel);
		pack();
	}
	
	public File getFlagFile() { return panel.getFlagFile(); }
	public File getParentFile() { return panel.getParentFile(); }
	public Flag getFlag() { return panel.getFlag(); }
	
	public void setFlag(File flagFile, File parent, Flag flag) {
		this.panel.setFlag(flagFile, parent, flag);
	}
	
	public int getViewerWidth() {
		return panel.getViewerWidth();
	}
	
	public int getViewerHeight() {
		return panel.getViewerHeight();
	}
	
	public void setHoist(int h) {
		Flag flag = panel.getFlag();
		int w = (
			(flag == null || flag.getFly() == null) ?
			(h * 3 / 2) : flag.getWidthFromHeight(h)
		);
		int xp = this.getWidth() - panel.getViewerWidth();
		int yp = this.getHeight() - panel.getViewerHeight();
		setSize(xp + w, yp + h);
	}
	
	public void setAspectRatio(int n, int d) {
		if (n < 1 || d < 1) {
			setHoist(panel.getViewerHeight());
		} else {
			int xp = this.getWidth() - panel.getViewerWidth();
			setSize(xp + panel.getViewerHeight() * n / d, this.getHeight());
		}
	}
	
	public boolean isGlazed() { return panel.isGlazed(); }
	public int getGlaze() { return panel.getGlaze(); }
	public int getGlazeAmount() { return panel.getGlazeAmount(); }
	public void setGlaze(boolean glaze) { panel.setGlaze(glaze); }
	public void setGlaze(boolean gl, int amt) { panel.setGlaze(gl, amt); }
	public void setGlaze(int amount) { panel.setGlaze(amount); }
}