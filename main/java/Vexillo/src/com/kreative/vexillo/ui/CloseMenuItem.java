package com.kreative.vexillo.ui;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class CloseMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public CloseMenuItem(final Window window) {
		setText("Close");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_C);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputUtils.META_MASK));
		if (window == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					window.dispose();
				}
			});
		}
	}
}