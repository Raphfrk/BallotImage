package com.raphfrk.ballotimage.gui;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.raphfrk.ballotimage.io.IDXFileReader;

public class GUIMainActionListener implements ActionListener {
	
	private final GUIMain parent;
	private boolean openingFile = false;
	
	private final Runnable fileReadComplete = new Runnable() {
		public void run() {
			openingFile = false;
		}
	};
	
	public GUIMainActionListener(GUIMain parent) {
		this.parent = parent;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("File-Exit")) {
			System.exit(0);
		} else if (e.getActionCommand().equals("File-Open")) {
			openFile();
		}
	}
	
	private void openFile() {
		if (openingFile) {
			JOptionPane.showMessageDialog(parent, "File open already in progress");
			return;
		} 
		openingFile = true;
		FileDialog fDialog = new FileDialog(parent, "Select File to Open", FileDialog.LOAD);

		fDialog.setVisible(true);

		String directory = fDialog.getDirectory();
		String filename = fDialog.getFile();
		if (directory == null || filename == null) {
			openingFile = false;
			return;
		}
		
		File file = new File(directory, filename);
		
		if (!file.exists()) {
			JOptionPane.showMessageDialog(parent, "File does not exist: " + file.getAbsolutePath(), "File Open Error", JOptionPane.ERROR_MESSAGE);
			openingFile = false;
			return;
		}
		
		new IDXFileReader(this, file).start();
		
	}
	
	public void fileReadComplete() {
		SwingUtilities.invokeLater(fileReadComplete);
	}
	
	public GUIMain getParent() {
		return parent;
	}
}
