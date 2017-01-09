package com.kreative.vexillo.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class GlazeDeltaMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public GlazeDeltaMenuItem(String text, int mn, int acc, final FlagFrame frame, final int gd) {
		setText(text);
		if (mn != 0 && !OSUtils.isMacOS()) setMnemonic(mn);
		if (acc != 0) setAccelerator(KeyStroke.getKeyStroke(acc, 0));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (frame.isGlazed()) {
						int g = frame.getGlazeAmount() + gd;
						if (g < 1) g = 1;
						if (g > 255) g = 255;
						frame.setGlaze(g);
					} else {
						frame.setGlaze(true);
					}
				}
			});
		}
	}
}