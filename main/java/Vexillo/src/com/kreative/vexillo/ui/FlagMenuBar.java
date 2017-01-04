package com.kreative.vexillo.ui;

import java.awt.Window;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

public class FlagMenuBar extends JMenuBar {
	private static final long serialVersionUID = 1L;
	
	public FlagMenuBar(Window window) {
		JMenu file = new JMenu("File");
		if (!OSUtils.isMacOS()) file.setMnemonic(KeyEvent.VK_F);
		file.add(new OpenMenuItem());
		file.add(new CloseMenuItem(window));
		if (!OSUtils.isMacOS()) {
			file.addSeparator();
			file.add(new ExitMenuItem());
		}
		add(file);
	}
}