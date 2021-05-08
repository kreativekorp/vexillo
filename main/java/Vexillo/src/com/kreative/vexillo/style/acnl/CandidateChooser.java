package com.kreative.vexillo.style.acnl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import com.kreative.vexillo.core.Flag;
import com.kreative.vexillo.core.FlagParser;
import com.kreative.vexillo.core.FlagRenderer;

public class CandidateChooser {
	public static void main(String[] args) throws IOException {
		final TreeMap<String,ButtonGroup> groups = new TreeMap<String,ButtonGroup>();
		final JPanel left = new JPanel(new GridLayout(0, 1));
		final JPanel right = new JPanel(new GridLayout(0, 1));
		
		for (String arg : args) {
			System.out.println(arg);
			File file = new File(arg);
			FileInputStream in = new FileInputStream(file);
			Flag flag = FlagParser.parse(file.getName(), in);
			FlagRenderer r = new FlagRenderer(file, file.getParentFile(), flag);
			in.close();
			
			BufferedImage srcImage = r.renderToImage(72, 72, null, 0, 0);
			JLabel srcLabel = new JLabel(new ImageIcon(srcImage));
			srcLabel.setText(file.getName());
			srcLabel.setHorizontalTextPosition(JLabel.CENTER);
			srcLabel.setVerticalTextPosition(JLabel.BOTTOM);
			srcLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			
			ButtonGroup group = new ButtonGroup();
			JPanel panel = new JPanel(new GridLayout(1, 0, 12, 12));
			panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			
			for (Candidate c : Candidate.createCandidates(r, 0)) {
				JLabel l = new JLabel(new ImageIcon(c.getPreview3x()));
				JRadioButton b = new JRadioButton(c.getParamString());
				JPanel p = new JPanel(new BorderLayout());
				p.add(l, BorderLayout.CENTER);
				p.add(b, BorderLayout.PAGE_END);
				group.add(b);
				panel.add(p);
			}
			
			groups.put(arg, group);
			left.add(srcLabel);
			right.add(new JScrollPane(
				panel,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
			));
		}
		
		JButton button = new JButton("Generate Script");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (Map.Entry<String,ButtonGroup> e : groups.entrySet()) {
					Enumeration<AbstractButton> buttons = e.getValue().getElements();
					while (buttons.hasMoreElements()) {
						AbstractButton b = buttons.nextElement();
						if (b.isSelected()) {
							System.out.println(
								"vexport -v -f png -y " +
								ACNLStylizer.class.getCanonicalName() +
								" -h 240 -w 400 " + b.getText() + " " +
								e.getKey()
							);
						}
					}
				}
			}
		});
		
		JPanel p1 = new JPanel(new BorderLayout());
		p1.add(left, BorderLayout.LINE_START);
		p1.add(right, BorderLayout.CENTER);
		
		JPanel p2 = new ScrollablePanel(new BorderLayout());
		p2.add(p1, BorderLayout.PAGE_START);
		
		JPanel p3 = new JPanel(new FlowLayout());
		p3.add(button);
		
		JPanel main = new JPanel(new BorderLayout());
		main.add(p3, BorderLayout.PAGE_END);
		main.add(new JScrollPane(
			p2,
			JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		), BorderLayout.CENTER);
		
		JFrame frame = new JFrame("Select the best candidate for each image:");
		frame.setContentPane(main);
		frame.setSize(900, 600);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	
	private static class ScrollablePanel extends JPanel implements Scrollable {
		private static final long serialVersionUID = 1L;
		public ScrollablePanel(LayoutManager layout) {
			super(layout);
		}
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
		@Override
		public int getScrollableBlockIncrement(Rectangle vr, int ori, int dir) {
			return vr.height;
		}
		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}
		@Override
		public int getScrollableUnitIncrement(Rectangle vr, int ori, int dir) {
			return 100;
		}
	}
}