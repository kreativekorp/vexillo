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
		view.add(new SetHoistMenuItem("Small", KeyEvent.VK_1, frame, 120));
		view.add(new SetHoistMenuItem("Medium", KeyEvent.VK_2, frame, 200));
		view.add(new SetHoistMenuItem("Large", KeyEvent.VK_3, frame, 360));
		view.addSeparator();
		view.add(new SetAspectRatioMenuItem("Automatic", KeyEvent.VK_A, frame, 0, 0));
		view.add(new SetAspectRatioMenuItem("3:2", KeyEvent.VK_C, frame, 3, 2));
		view.add(new SetAspectRatioMenuItem("4:3", KeyEvent.VK_D, frame, 4, 3));
		view.add(new SetAspectRatioMenuItem("1:1", KeyEvent.VK_F, frame, 1, 1));
		view.add(new SetAspectRatioMenuItem("2:1", KeyEvent.VK_H, frame, 2, 1));
		view.add(new SetAspectRatioMenuItem("16:11", KeyEvent.VK_K, frame, 16, 11));
		view.add(new SetAspectRatioMenuItem("16:10", KeyEvent.VK_V, frame, 16, 10));
		view.add(new SetAspectRatioMenuItem("16:9", KeyEvent.VK_W, frame, 16, 9));
		view.addSeparator();
		view.add(new RefreshMenuItem(frame, flagFile));
		add(view);
	}
}