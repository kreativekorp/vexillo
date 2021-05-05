package com.kreative.vexillo.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import com.kreative.vexillo.core.FlagRenderer;

public class CopyImageMenuItem extends JMenuItem {
	private static final long serialVersionUID = 1L;
	
	public CopyImageMenuItem(final FlagFrame frame) {
		setText("Copy Image");
		if (!OSUtils.isMacOS()) setMnemonic(KeyEvent.VK_C);
		setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputUtils.META_MASK));
		if (frame == null) {
			setEnabled(false);
		} else {
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FlagRenderer r = new FlagRenderer(frame.getFlagFile(), frame.getFlag());
					BufferedImage img = r.renderToImage(
						frame.getViewerWidth(),
						frame.getViewerHeight(),
						null, 0, frame.getGlaze()
					);
					ImageSelection is = new ImageSelection(img);
					Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
					cb.setContents(is, is);
				}
			});
		}
	}
	
	private static final class ImageSelection implements ClipboardOwner, Transferable {
		private final BufferedImage image;
		public ImageSelection(BufferedImage image) {
			this.image = image;
		}
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (DataFlavor.imageFlavor.equals(flavor)) {
				return image;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (DataFlavor.imageFlavor.equals(flavor));
		}
		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			//nothing
		}
	}
}