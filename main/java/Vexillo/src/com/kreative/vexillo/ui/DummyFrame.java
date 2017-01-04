package com.kreative.vexillo.ui;

import java.awt.Dimension;
import javax.swing.JFrame;

public class DummyFrame extends JFrame {
	public static final long serialVersionUID = 1L;
	
	public DummyFrame() {
		setJMenuBar(new FlagMenuBar(null));
		setUndecorated(true);
		setResizable(false);
		setMinimumSize(new Dimension(0,0));
		setPreferredSize(new Dimension(0,0));
		setMaximumSize(new Dimension(0,0));
		setSize(new Dimension(0,0));
		setLocation(-1000000, -1000000);
		setVisible(true);
	}
}