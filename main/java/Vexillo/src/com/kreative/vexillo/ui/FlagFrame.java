package com.kreative.vexillo.ui;

import java.io.File;
import javax.swing.JFrame;
import com.kreative.vexillo.core.Flag;

public class FlagFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final FlagPanel panel;
	private File flagFile;
	private Flag flag;
	
	public FlagFrame(String title, File flagFile, Flag flag) {
		super(title);
		setJMenuBar(new FlagMenuBar(this));
		this.panel = new FlagPanel(flagFile.getParentFile(), flag);
		this.flagFile = flagFile;
		this.flag = flag;
		setContentPane(panel);
		pack();
	}
	
	public File getParentFile() {
		return flagFile.getParentFile();
	}
	
	public File getFlagFile() {
		return flagFile;
	}
	
	public Flag getFlag() {
		return flag;
	}
	
	public void setFlag(String title, File flagFile, Flag flag) {
		setTitle(title);
		this.panel.setFlag(flagFile.getParentFile(), flag);
		this.flagFile = flagFile;
		this.flag = flag;
	}
	
	public int getViewerWidth() {
		return panel.getViewerWidth();
	}
	
	public int getViewerHeight() {
		return panel.getViewerHeight();
	}
	
	public void setHoist(int h) {
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