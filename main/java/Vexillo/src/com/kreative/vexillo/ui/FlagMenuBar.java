package com.kreative.vexillo.ui;

import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class FlagMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
	public FlagMenuBar(FlagFrame frame, File flagFile) {
		JMenu file = new JMenu("File");
		if (!OSUtils.isMacOS()) file.setMnemonic(KeyEvent.VK_F);
		file.add(new OpenMenuItem());
		file.add(new CloseMenuItem(frame));
		if (!OSUtils.isMacOS()) {
			file.addSeparator();
			file.add(new ExitMenuItem());
		}
		add(file);
		
		JMenu view = new JMenu("View");
		if (!OSUtils.isMacOS()) view.setMnemonic(KeyEvent.VK_V);
		view.add(new RefreshMenuItem(frame, flagFile));
		add(view);
	}
}