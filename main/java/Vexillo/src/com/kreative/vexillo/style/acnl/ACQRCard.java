package com.kreative.vexillo.style.acnl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ACQRCard {
	private static final int WIDTH = 400;
	private static final int HEIGHT = 240;
	private static final Color BACKGROUND_COLOR = new Color(0xFAFAFA, false);
	private static final Color HATCH_COLOR = new Color(0x99C08597, true);
	private static final BasicStroke HATCH_STROKE = new BasicStroke(16, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10);
	private static final int HATCH_SPACING = 22;
	private static final Color TITLE_BAR_COLOR = new Color(0x81375C, false);
	private static final Rectangle TITLE_BAR_RECT = new Rectangle(64, 2, 272, 30);
	private static final int TITLE_BAR_RADIUS = 30;
	private static final Color TITLE_TEXT_COLOR = new Color(0xE3D5B0, false);
	private static final Font TITLE_TEXT_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 18);
	private static final int TITLE_TEXT_Y = 24;
	private static final int PREVIEW_CENTER_X = 94;
	private static final int PREVIEW_CENTER_Y = 120;
	private static final int QR_SCALE = 2;
	private static final int QR_CENTER_X = 285;
	private static final int QR_CENTER_Y = 135;
	private static final Color PAGENUM_STROKE_COLOR = new Color(0x05B1C5, false);
	private static final Color PAGENUM_FILL_COLOR = new Color(0x6CDFDE, false);
	private static final Rectangle PAGENUM_RECT = new Rectangle(-40, 200, 110, 60);
	private static final int PAGENUM_RADIUS = 20;
	private static final BasicStroke PAGENUM_STROKE = new BasicStroke(4);
	private static final Color PAGENUM_TEXT_SHADOW = new Color(0xE8F4F2, false);
	private static final Color PAGENUM_TEXT_COLOR = new Color(0x5B170E, false);
	private static final Font PAGENUM_TEXT_FONT = new Font("Arial Rounded MT Bold", Font.BOLD, 32);
	private static final int PAGENUM_TEXT_X = 32;
	private static final int PAGENUM_TEXT_Y = 234;
	
	private final String title;
	private final BufferedImage preview;
	private final ACQRCode qrcode;
	private final BufferedImage qrimage;
	private final BufferedImage qrcard;
	
	public ACQRCard(String title, BufferedImage preview, int previewScale, ACQRCode qrcode) {
		this.title = title;
		this.preview = preview;
		this.qrcode = qrcode;
		this.qrimage = qrcode.getMatrixImage();
		this.qrcard = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = qrcard.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Background
		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setColor(HATCH_COLOR);
		g.setStroke(HATCH_STROKE);
		for (int x = -(HEIGHT+HATCH_SPACING); x < WIDTH+HATCH_SPACING; x += HATCH_SPACING+HATCH_SPACING) {
			g.drawLine(x, 0, x + HEIGHT, HEIGHT);
			g.drawLine(x, HEIGHT, x + HEIGHT, 0);
		}
		
		// Title
		g.setColor(TITLE_BAR_COLOR);
		g.fillRoundRect(TITLE_BAR_RECT.x, TITLE_BAR_RECT.y, TITLE_BAR_RECT.width, TITLE_BAR_RECT.height, TITLE_BAR_RADIUS, TITLE_BAR_RADIUS);
		g.setColor(TITLE_TEXT_COLOR);
		g.setFont(TITLE_TEXT_FONT);
		FontMetrics fm = g.getFontMetrics();
		int tx = TITLE_BAR_RECT.x + (TITLE_BAR_RECT.width - fm.stringWidth(title)) / 2;
		g.drawString(title, tx, TITLE_TEXT_Y);
		
		// Preview
		if (preview != null && previewScale > 0) {
			int pw = preview.getWidth() * previewScale;
			int ph = preview.getHeight() * previewScale;
			int px = PREVIEW_CENTER_X - pw / 2;
			int py = PREVIEW_CENTER_Y - ph / 2;
			g.drawImage(preview, px, py, pw, ph, null);
		}
		
		// QR Code
		int cw = qrimage.getWidth() * QR_SCALE;
		int ch = qrimage.getHeight() * QR_SCALE;
		int cx = QR_CENTER_X - cw / 2;
		int cy = QR_CENTER_Y - ch / 2;
		g.drawImage(qrimage, cx, cy, cw, ch, null);
		
		// Page Number
		if (qrcode.getFrameCount() > 1) {
			String pagenum = (qrcode.getFrameIndex() + 1) + "/" + qrcode.getFrameCount();
			g.setColor(PAGENUM_FILL_COLOR);
			g.fillRoundRect(PAGENUM_RECT.x, PAGENUM_RECT.y, PAGENUM_RECT.width, PAGENUM_RECT.height, PAGENUM_RADIUS, PAGENUM_RADIUS);
			g.setColor(PAGENUM_STROKE_COLOR);
			g.setStroke(PAGENUM_STROKE);
			g.drawRoundRect(PAGENUM_RECT.x, PAGENUM_RECT.y, PAGENUM_RECT.width, PAGENUM_RECT.height, PAGENUM_RADIUS, PAGENUM_RADIUS);
			g.setFont(PAGENUM_TEXT_FONT);
			fm = g.getFontMetrics();
			int x = PAGENUM_TEXT_X - fm.stringWidth(pagenum) / 2;
			g.setColor(PAGENUM_TEXT_SHADOW);
			g.drawString(pagenum, x + 2, PAGENUM_TEXT_Y + 1);
			g.setColor(PAGENUM_TEXT_COLOR);
			g.drawString(pagenum, x, PAGENUM_TEXT_Y);
		}
		
		g.dispose();
	}
	
	public String getTitle() {
		return title;
	}
	
	public BufferedImage getPreview() {
		return preview;
	}
	
	public ACQRCode getQRCode() {
		return qrcode;
	}
	
	public BufferedImage getQRImage() {
		return qrimage;
	}
	
	public BufferedImage getCardImage() {
		return qrcard;
	}
}
