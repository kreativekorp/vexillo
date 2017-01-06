package com.kreative.vexillo.ui;

import java.io.File;
import javax.swing.JFrame;
import com.kreative.vexillo.core.DimensionUtils;
import com.kreative.vexillo.core.Flag;

public class FlagFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final FlagPanel panel;
	
	public FlagFrame(String title, File flagFile, Flag flag) {
		super(title);
		setJMenuBar(new FlagMenuBar(this, flagFile));
		panel = new FlagPanel(flagFile.getParentFile(), flag);
		setContentPane(panel);
		pack();
	}
	
	public void setFlag(File parent, Flag flag) {
		panel.setFlag(parent, flag);
		setHoist(panel.getViewerHeight());
	}
	
	public void setHoist(int h) {
		Flag flag = panel.getFlag();
		int w = (
			(flag == null || flag.getFly() == null) ? (h * 3 / 2) :
			(int)Math.round(flag.getFly().value(
				DimensionUtils.createNamespace(flag.dimensions(), h)
			))
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
}