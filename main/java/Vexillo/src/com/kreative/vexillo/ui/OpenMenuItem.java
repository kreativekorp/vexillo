package com.kreative.vexillo.ui;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;

public class OpenMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public OpenMenuItem() {
		setText("Open...");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_O);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputUtils.META_MASK));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				FileDialog fd = new FileDialog(new Frame(), "Open", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getDirectory() == null || fd.getFile() == null) return;
				File file = new File(fd.getDirectory(), fd.getFile());
				try {
					FileInputStream in = new FileInputStream(file);
					Flag flag = FlagParser.parse(file.getName(), in);
					in.close();
					String title = file.getName();
					if (flag.getName() != null) title += ": " + flag.getName();
					FlagFrame frame = new FlagFrame(title, file, file.getParentFile(), flag);
					frame.setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Open", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
}