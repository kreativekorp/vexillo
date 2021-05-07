package com.kreative.vexillo.ui;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JPanel;
import com.kreative.vexillo.core.Flag;

public class FlagPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final FlagInfoPanel infoPanel;
	private final FlagViewer viewer;
	
	public FlagPanel(File flagFile, File parent, Flag flag) {
		this.infoPanel = new FlagInfoPanel(flag);
		this.viewer = new FlagViewer(flagFile, parent, flag);
		setLayout(new BorderLayout());
		add(infoPanel, BorderLayout.PAGE_START);
		add(viewer, BorderLayout.CENTER);
	}
	
	public File getFlagFile() { return viewer.getFlagFile(); }
	public File getParentFile() { return viewer.getParentFile(); }
	public Flag getFlag() { return viewer.getFlag(); }
	
	public void setFlag(File flagFile, File parent, Flag flag) {
		this.infoPanel.setFlag(flag);
		this.viewer.setFlag(flagFile, parent, flag);
	}
	
	public int getViewerWidth() { return viewer.getWidth(); }
	public int getViewerHeight() { return viewer.getHeight(); }
	public boolean isGlazed() { return viewer.isGlazed(); }
	public int getGlaze() { return viewer.getGlaze(); }
	public int getGlazeAmount() { return viewer.getGlazeAmount(); }
	public void setGlaze(boolean glaze) { viewer.setGlaze(glaze); }
	public void setGlaze(boolean gl, int amt) { viewer.setGlaze(gl, amt); }
	public void setGlaze(int amount) { viewer.setGlaze(amount); }
}