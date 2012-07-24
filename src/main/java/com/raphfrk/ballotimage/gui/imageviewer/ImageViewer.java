package com.raphfrk.ballotimage.gui.imageviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.raphfrk.ballotimage.io.vararray.VarByteArray;

public class ImageViewer extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int ZOOM = 16;
	private static final int IMAGE_SIZE = 512;
	
	private final float[][][] images;
	private final int[] labels;
	private int currentImage = 0;
	private final ImagePanel imagePanel;
	private final JTextField positionBox;
	private final JLabel noteLabel;

	public ImageViewer(VarByteArray labels, VarByteArray images) {
		int s0 = images.size(0);
		int s1 = images.size(1);
		int s2 = images.size(2);
		
		this.images = new float[s0][s1][s2];
		
		for (int i0 = 0; i0 < s0; i0++) {
			for (int i1 = 0; i1 < s1; i1++) {
				for (int i2 = 0; i2 < s2; i2++) {
					this.images[i0][i1][i2] = images.get(i0, i1, i2) & 0xFF;
				}
			}
		}
		
		this.labels = new int[labels.size()];
		
		for (int i = 0; i < this.labels.length; i++) {
			this.labels[i] = labels.get(i);
		}
		
		ActionListener listener = new ImageViewerActionListener(this);
		
		setLayout(new BorderLayout());
		
		imagePanel = new ImagePanel(ImageUtils.zoomNearest(this.images[0], ZOOM));
		imagePanel.setPreferredSize(new Dimension(IMAGE_SIZE, IMAGE_SIZE));
		
		add(imagePanel, BorderLayout.NORTH);
		
		noteLabel = new JLabel();
		noteLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(noteLabel, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		JButton prev = new JButton("\u22b2 Prev");
		prev.addActionListener(listener);
		prev.setActionCommand("Prev");
		positionBox = new JTextField(6);
		positionBox.setHorizontalAlignment(JTextField.RIGHT);
		positionBox.addActionListener(listener);
		positionBox.setActionCommand("PositionText");
		JLabel total = new JLabel(" / " + this.images.length);
		updatePosition();
		JButton next = new JButton("Next \u22b3");
		next.addActionListener(listener);
		next.setActionCommand("Next");
		buttons.add(prev, BorderLayout.WEST);
		buttons.add(positionBox, BorderLayout.CENTER);
		buttons.add(total);
		buttons.add(next, BorderLayout.EAST);
		
		add(buttons, BorderLayout.SOUTH);
		
		setImage(1);
	}
	
	public void moveImageRaw(final int offset) {
		int newImage = this.currentImage + offset;
		if (newImage >= 0 && newImage < images.length) {
			setImageRaw(newImage);
		} else if (newImage < 0) {
			setImageRaw(0);
		} else if (newImage >= images.length) {
			setImage(images.length - 1);
		}
	}
	
	public void refreshPositionRaw() {
		String text = positionBox.getText();
		try {
			setImageRaw(Integer.parseInt(text));
			
		} catch (NumberFormatException nfe) {
		}
	}
	
	public void setImage(final int index) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setImageRaw(index);
			}
		});
	}
	
	private void setImageRaw(int index) {
		if (index < 0 || index >= images.length) {
			setImage(index, 0, null);
		} else {
			setImage(index, labels[index], images[index]);
		}
	}
	
	private void updatePosition() {
		positionBox.setText(Integer.toString(currentImage + 1));
	}
	
	private void setImage(int index, int label, float[][] imageData) {
		currentImage = index;
		noteLabel.setText("Label: " + label);
		imageData = ImageUtils.splineFit(3, imageData, IMAGE_SIZE, IMAGE_SIZE);
		imagePanel.setImage(imageData);
		updatePosition();
		this.revalidate();
	}
	
}
