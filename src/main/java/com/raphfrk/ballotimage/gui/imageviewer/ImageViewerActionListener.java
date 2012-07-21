package com.raphfrk.ballotimage.gui.imageviewer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImageViewerActionListener implements ActionListener {

	private final ImageViewer parent;
	
	public ImageViewerActionListener(ImageViewer parent) {
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Next")) {
			parent.moveImageRaw(+1);
		} else if (e.getActionCommand().equals("Prev")) {
			parent.moveImageRaw(-1);
		} else if (e.getActionCommand().equals("PositionText")) {
			parent.refreshPositionRaw();
		}
	}

}
