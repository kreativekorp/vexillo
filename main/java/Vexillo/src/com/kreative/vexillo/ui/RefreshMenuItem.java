package com.kreative.vexillo.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;

public class RefreshMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public RefreshMenuItem(final FlagFrame frame) {
		setText("Refresh");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_R);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputUtils.META_MASK));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					try {
						File flagFile = frame.getFlagFile();
						File parentFile = frame.getParentFile();
						FileInputStream in = new FileInputStream(flagFile);
						Flag flag = FlagParser.parse(flagFile.getName(), in);
						in.close();
						String title = flagFile.getName();
						if (flag.getName() != null) title += ": " + flag.getName();
						frame.setTitle(title);
						frame.setFlag(flagFile, parentFile, flag);
					} catch (Exception e) {}
				}
			});
		}
	}
}