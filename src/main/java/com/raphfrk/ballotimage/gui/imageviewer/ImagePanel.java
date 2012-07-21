package com.raphfrk.ballotimage.gui.imageviewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImagePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private BufferedImage img = null;
	
	public ImagePanel(byte[][] brightness) {
		setImageRaw(brightness);
		setBorder(BorderFactory.createEtchedBorder());
		setVisible(true);
	}

	public void setImage(final byte[][] brightness) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setImageRaw(brightness);
			}
		});
	}
	
	private void setImageRaw(byte[][] brightness) {
		int sy = brightness.length;
		int sx = brightness[0].length;
		
		setPreferredSize(new Dimension(sx, sy));
		
		BufferedImage img = new BufferedImage(sx, sy, BufferedImage.TYPE_BYTE_GRAY);
		
		for (int y = 0; y < sy; y++) {
			for (int x = 0; x < sx; x++) {
				int br = 255 - brightness[y][x];
				int r = br & 0xFF;
				int g = br & 0xFF;
				int b = br & 0xFF;
				br = r << 16 | g << 8 | b << 0;
				img.setRGB(x, y, br);
			}
		}
		
		this.img = img;
		this.repaint();
	}
	
	 public void paintComponent(Graphics g) {
         super.paintComponent(g);

         ((Graphics2D) g).drawImage(img, 0, 0, null);
     } 
	
}
