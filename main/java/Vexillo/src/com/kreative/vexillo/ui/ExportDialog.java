package com.kreative.vexillo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagRenderer;
import com.kreative.vexillo.core.SVGExporter;

public class ExportDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private static final String[] FORMATS = {"BMP","GIF","JPEG","PNG","SVG"};
	private static final int HGAP = 8;
	private static final int VGAP = 4;
	private static final int HBORDER = 20;
	private static final int VBORDER = 12;
	
	private final File parentFile;
	private final Flag flag;
	private final File outputFile;
	
	private JComboBox formatSelector;
	private SpinnerNumberModel widthSpinnerModel;
	private SpinnerNumberModel heightSpinnerModel;
	private JSpinner widthSpinner;
	private JSpinner heightSpinner;
	private JCheckBox constrainCheckBox;
	private SpinnerNumberModel supersampleSpinnerModel;
	private JSpinner supersampleSpinner;
	private SpinnerNumberModel glazeSpinnerModel;
	private JSpinner glazeSpinner;
	private JButton cancelButton;
	private JButton saveButton;
	private boolean locked = false;
	
	public ExportDialog(Frame p, File pf, Flag flag, File out, int w, int h, int g) {
		super(p, "Export", true);
		this.parentFile = pf;
		this.flag = flag;
		this.outputFile = out;
		makeGUI(w, h, g);
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}
	
	private void makeGUI(int w, int h, int g) {
		formatSelector = new JComboBox(FORMATS);
		formatSelector.setEditable(false);
		formatSelector.setMaximumRowCount(FORMATS.length);
		String fn = outputFile.getName().toLowerCase();
		if (fn.endsWith(".svg")) formatSelector.setSelectedItem("SVG");
		else if (fn.endsWith(".bmp")) formatSelector.setSelectedItem("BMP");
		else if (fn.endsWith(".gif")) formatSelector.setSelectedItem("GIF");
		else if (fn.endsWith(".jpeg")) formatSelector.setSelectedItem("JPEG");
		else if (fn.endsWith(".jpg")) formatSelector.setSelectedItem("JPEG");
		else formatSelector.setSelectedItem("PNG");
		
		widthSpinnerModel = new SpinnerNumberModel(w, 1, 32767, 1);
		heightSpinnerModel = new SpinnerNumberModel(h, 1, 32767, 1);
		widthSpinner = new JSpinner(widthSpinnerModel);
		heightSpinner = new JSpinner(heightSpinnerModel);
		constrainCheckBox = new JCheckBox("Constrain Proportions");
		supersampleSpinnerModel = new SpinnerNumberModel(1, 1, 255, 1);
		supersampleSpinner = new JSpinner(supersampleSpinnerModel);
		glazeSpinnerModel = new SpinnerNumberModel(g, 0, 255, 1);
		glazeSpinner = new JSpinner(glazeSpinnerModel);
		cancelButton = new JButton("Cancel");
		saveButton = new JButton("Export");
		
		JPanel p1 = makeRow(formatSelector);
		JPanel p2 = makeRow(widthSpinner, "px  by  ", heightSpinner, "px");
		JPanel p3 = makeRow(constrainCheckBox);
		JPanel p4 = makeRow(supersampleSpinner, "x");
		JPanel p5 = makeRow(glazeSpinner, "px");
		JPanel bottom = new JPanel(new FlowLayout());
		bottom.add(cancelButton);
		bottom.add(saveButton);
		
		JPanel left = new JPanel(new GridLayout(0,1,HGAP,VGAP));
		left.add(new JLabel("Format:"));
		left.add(new JLabel("Size:"));
		left.add(new JLabel(" "));
		left.add(new JLabel("Supersampling:"));
		left.add(new JLabel("Glazing:"));
		JPanel right = new JPanel(new GridLayout(0,1,HGAP,VGAP));
		right.add(p1);
		right.add(p2);
		right.add(p3);
		right.add(p4);
		right.add(p5);
		JPanel top = new JPanel(new BorderLayout(HGAP,VGAP));
		top.add(left, BorderLayout.LINE_START);
		top.add(right, BorderLayout.CENTER);
		
		JPanel main = new JPanel(new BorderLayout(HGAP,VGAP));
		main.add(top, BorderLayout.CENTER);
		main.add(bottom, BorderLayout.PAGE_END);
		main.setBorder(BorderFactory.createEmptyBorder(VBORDER,HBORDER,VBORDER,HBORDER));
		setContentPane(main);
		getRootPane().setDefaultButton(saveButton);
		setCancelButton(getRootPane(), cancelButton);
		
		widthSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (locked) return;
				if (constrainCheckBox.isSelected()) {
					int w = widthSpinnerModel.getNumber().intValue();
					int h = flag.getHeightFromWidth(w);
					locked = true;
					heightSpinnerModel.setValue(h);
					locked = false;
				}
			}
		});
		
		heightSpinnerModel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (locked) return;
				if (constrainCheckBox.isSelected()) {
					int h = heightSpinnerModel.getNumber().intValue();
					int w = flag.getWidthFromHeight(h);
					locked = true;
					widthSpinnerModel.setValue(w);
					locked = false;
				}
			}
		});
		
		constrainCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (locked) return;
				if (constrainCheckBox.isSelected()) {
					int h = heightSpinnerModel.getNumber().intValue();
					int w = flag.getWidthFromHeight(h);
					locked = true;
					widthSpinnerModel.setValue(w);
					locked = false;
				}
			}
		});
		
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportDialog.this.dispose();
			}
		});
		
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				ExportDialog.this.dispose();
				try {
					String format = formatSelector.getSelectedItem().toString().toLowerCase();
					int width = widthSpinnerModel.getNumber().intValue();
					int height = heightSpinnerModel.getNumber().intValue();
					int ss = supersampleSpinnerModel.getNumber().intValue();
					int glaze = glazeSpinnerModel.getNumber().intValue();
					if (format.equals("svg")) {
						SVGExporter e = new SVGExporter(parentFile, flag);
						e.export(outputFile, width, height, glaze);
					} else {
						FlagRenderer r = new FlagRenderer(parentFile, flag);
						r.renderToFile(outputFile, format, width, height, ss, glaze);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Export", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	private static JPanel makeRow(Object... cc) {
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.LINE_AXIS));
		for (int i = 0; i < cc.length; i++) {
			if (i > 0) p1.add(Box.createHorizontalStrut(HGAP));
			if (cc[i] instanceof Component) p1.add((Component)cc[i]);
			else p1.add(new JLabel(cc[i].toString()));
		}
		JPanel p2 = new JPanel(new BorderLayout());
		p2.add(p1, BorderLayout.LINE_START);
		return p2;
	}
	
	private static void setCancelButton(final JRootPane rp, final JButton b) {
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		rp.getActionMap().put("cancel", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent ev) {
				b.doClick();
			}
		});
	}
}