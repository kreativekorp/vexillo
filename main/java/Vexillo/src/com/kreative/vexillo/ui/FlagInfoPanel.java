package com.kreative.vexillo.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.InputStream;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.PropertySet;

public class FlagInfoPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private static final int HGAP = 8;
	private static final int VGAP = 4;
	private static final int HBORDER = 12;
	private static final int VBORDER = 8;
	private static final Font ID_FONT = new Font("Monospaced", Font.PLAIN, 12);
	private static final Font ICON_FONT = getFont("Fiavex.ttf", 16);
	
	private final JLabel idLabel;
	private final JLabel nameLabel;
	private final JLabel propertyLabel;
	private final JLabel proportionLabel;
	private Flag flag;
	
	public FlagInfoPanel() {
		this(null);
	}
	
	public FlagInfoPanel(Flag flag) {
		JPanel labelPanel = new JPanel(new GridLayout(0,1,HGAP,VGAP));
		labelPanel.add(new JLabel("ID:"));
		labelPanel.add(new JLabel("Name:"));
		labelPanel.add(new JLabel("Properties:"));
		labelPanel.add(new JLabel("Proportion:"));
		JPanel infoPanel = new JPanel(new GridLayout(0,1,HGAP,VGAP));
		infoPanel.add(idLabel = new JLabel());
		infoPanel.add(nameLabel = new JLabel());
		infoPanel.add(propertyLabel = new JLabel());
		infoPanel.add(proportionLabel = new JLabel());
		idLabel.setFont(ID_FONT);
		propertyLabel.setFont(ICON_FONT);
		setLayout(new BorderLayout(HGAP,VGAP));
		add(labelPanel, BorderLayout.LINE_START);
		add(infoPanel, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(VBORDER,HBORDER,VBORDER,HBORDER));
		setFlag(flag);
	}
	
	public Flag getFlag() {
		return flag;
	}
	
	public void setFlag(Flag flag) {
		this.flag = flag;
		if (flag != null) {
			idLabel.setText(flag.getId());
			nameLabel.setText(flag.getName());
			propertyLabel.setText(propertyString());
			proportionLabel.setText(flag.getProportionString());
		}
	}
	
	private String propertyString() {
		PropertySet p = flag.getProperties();
		if (p != null) {
			String s = p.getCodePointString();
			if (s != null && s.length() > 0) {
				return s;
			}
		}
		return "\u00A0";
	}
	
	private static Font getFont(String name, int size) {
		try {
			InputStream in = FlagInfoPanel.class.getResourceAsStream(name);
			Font font = Font.createFont(Font.TRUETYPE_FONT, in);
			return font.deriveFont((float)size);
		} catch (Exception e) {
			return null;
		}
	}
}