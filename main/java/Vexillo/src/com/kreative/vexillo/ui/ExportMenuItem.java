package com.kreative.vexillo.ui;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ExportMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public ExportMenuItem(final FlagFrame frame) {
		setText("Export...");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_E);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputUtils.META_MASK));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					FileDialog fd = new FileDialog(frame, "Export", FileDialog.SAVE);
					fd.setVisible(true);
					if (fd.getDirectory() == null || fd.getFile() == null) return;
					File file = new File(fd.getDirectory(), fd.getFile());
					new ExportDialog(
						frame, frame.getParentFile(), frame.getFlag(), file,
						frame.getViewerWidth(), frame.getViewerHeight(), frame.getGlaze()
					).setVisible(true);
				}
			});
		}
	}
}