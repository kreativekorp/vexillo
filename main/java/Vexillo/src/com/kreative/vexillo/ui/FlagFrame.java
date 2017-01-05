package com.kreative.vexillo.ui;

import java.io.File;
import javax.swing.JFrame;
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
		pack();
	}
}