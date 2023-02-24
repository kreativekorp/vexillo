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
	
	private static String lastOpenDirectory = null;
	
	public OpenMenuItem() {
		setText("Open...");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_O);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputUtils.META_MASK));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Frame frame = new Frame();
				FileDialog fd = new FileDialog(frame, "Open", FileDialog.LOAD);
				if (lastOpenDirectory != null) fd.setDirectory(lastOpenDirectory);
				fd.setVisible(true);
				String ds = fd.getDirectory(), fs = fd.getFile();
				fd.dispose();
				frame.dispose();
				if (ds == null || fs == null) return;
				File file = new File((lastOpenDirectory = ds), fs);
				try {
					FileInputStream in = new FileInputStream(file);
					Flag flag = FlagParser.parse(file.getName(), in);
					in.close();
					String title = file.getName();
					if (flag.getName() != null) title += ": " + flag.getName();
					new FlagFrame(title, file, file.getParentFile(), flag).setVisible(true);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Open", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
}