package com.kreative.vexillo.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import com.kreative.vexillo.core.SVGExporter;

public class CopySVGMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public CopySVGMenuItem(final FlagFrame frame) {
		setText("Copy SVG");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_S);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputUtils.META_SHIFT_MASK));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					SVGExporter s = new SVGExporter(frame.getFlagFile(), frame.getParentFile(), frame.getFlag());
					String svg = s.exportToString(frame.getViewerWidth(), frame.getViewerHeight(), frame.getGlaze());
					StringSelection ss = new StringSelection(svg);
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					cb.setContents(ss, ss);
				}
			});
		}
	}
}