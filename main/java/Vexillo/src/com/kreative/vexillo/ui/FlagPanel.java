package com.kreative.vexillo.ui;

import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JPanel;
import com.kreative.vexillo.core.Flag;

public class FlagPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private final FlagInfoPanel infoPanel;
	private final FlagViewer viewer;
	private File parent;
	private Flag flag;
	
	public FlagPanel() {
		this(null, null);
	}
	
	public FlagPanel(File parent, Flag flag) {
		this.infoPanel = new FlagInfoPanel(flag);
		this.viewer = new FlagViewer(parent, flag);
		this.parent = parent;
		this.flag = flag;
		setLayout(new BorderLayout());
		add(infoPanel, BorderLayout.PAGE_START);
		add(viewer, BorderLayout.CENTER);
	}
	
	public File getParentFile() {
		return parent;
	}
	
	public Flag getFlag() {
		return flag;
	}
	
	public void setFlag(File parent, Flag flag) {
		this.infoPanel.setFlag(flag);
		this.viewer.setFlag(parent, flag);
		this.parent = parent;
		this.flag = flag;
	}
	
	public int getViewerWidth() {
		return viewer.getWidth();
	}
	
	public int getViewerHeight() {
		return viewer.getHeight();
	}
}