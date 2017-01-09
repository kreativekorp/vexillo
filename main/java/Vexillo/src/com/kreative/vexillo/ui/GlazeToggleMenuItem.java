package com.kreative.vexillo.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

public class GlazeToggleMenuItem extends JCheckBoxMenuItem {
	private static final long serialVersionUID = 1L;
	
	public GlazeToggleMenuItem(final FlagFrame frame) {
		setText("Glaze");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_G);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SLASH, 0));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					boolean g = !frame.isGlazed();
					frame.setGlaze(g);
					setSelected(g);
				}
			});
		}
	}
}