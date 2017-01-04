package com.kreative.vexillo.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ExitMenuItem  extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public ExitMenuItem() {
		setText("Exit");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_X);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputUtils.META_MASK));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
	}
}