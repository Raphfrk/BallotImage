package com.raphfrk.ballotimage.io;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.raphfrk.ballotimage.gui.GUIMainActionListener;
import com.raphfrk.ballotimage.gui.imageviewer.ImageViewer;
import com.raphfrk.ballotimage.io.vararray.VarByteArray;

public class IDXFileReader extends Thread {
	
	private final GUIMainActionListener listener;
	private final File file;
	
	public IDXFileReader(GUIMainActionListener listener, File file) {
		this.listener = listener;
		this.file = file;
	}
	
	public void run() {
		
		String statusText = "Reading file: " + file.getName();
		
		listener.getParent().setStatusBar(statusText);
		
		try {
			
			String filename = file.getName();
			String imageFilename = null;
			String labelFilename = null;
			
			if (filename.contains("images") && filename.contains("idx3") && filename.endsWith(".gz")) {
				imageFilename = filename;
				labelFilename = filename.replace("images", "labels").replace("idx3", "idx1");
			} else if (filename.contains("labels") && filename.contains("idx1") && filename.endsWith(".gz")) {
				labelFilename = filename;
				imageFilename = filename.replace("labels", "images").replace("idx1", "idx3");
			} else {
				safeMessage("Unable to open file", "Filename must contain \"labels\" or \"images\" and be of type .gz", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			File imageFile = new File(file.getParent(), imageFilename);
			File labelFile = new File(file.getParent(), labelFilename);
			
			if (!labelFile.exists()) {
				safeMessage("Unable to open file", "Label file does not exist in the directory: " + labelFile.getName(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			if (!imageFile.exists()) {
				safeMessage("Unable to open file", "Image file does not exist in the directory: " + imageFile.getName(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			VarByteArray imageArray = readIDX(imageFile);
			VarByteArray labelArray = readIDX(labelFile);
			
			if (imageArray.size() != labelArray.size()) {
				safeMessage("File read error", "The image and label files have different record lengths: " + imageArray.size() + " " + labelArray.size(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			createImagePanel(labelArray, imageArray);
			
		} finally {
			listener.getParent().unsetStatusBar(statusText);
			listener.fileReadComplete();
		}
		
	}
	
	private VarByteArray readIDX(File file) {
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException fnf) {
			safeMessage("Unable to open file", "File not found: " + file.getName(), JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		DataInputStream in = null;
		try {
			in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(fis)));
			
			int magic1 = in.readByte() & 0xFF;
			int magic2 = in.readByte() & 0xFF;
			
			if (magic1 != 0 || magic2 != 0) {
				safeMessage("Invalid magic number", "File contains non-byte data: " + file.getName(), JOptionPane.ERROR_MESSAGE);
				return null;
			}
			int type = in.readByte() & 0xFF;
			if (type != 8) {
				safeMessage("Unable to process file", "File contains non-byte data: " + file.getName(), JOptionPane.ERROR_MESSAGE);
				return null;
			}
			int dimensions = in.readByte() & 0xFF;
			
			int[] sizes = new int[dimensions];
			
			for (int i = 0; i < dimensions; i++) {
				sizes[i] = in.readInt();
			}
			
			VarByteArray varArray = VarByteArray.newInstance(sizes);
			
			varArray.read(in);
			
			return varArray;
			
		} catch (EOFException eof) {
			safeMessage("Unable to process file", "Unexpected EOF when reading file: " + file.getName(), JOptionPane.ERROR_MESSAGE);
			return null;
		} catch (IOException ioe) {
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
				}
			}
		}
		
	}
	
	private void createImagePanel(final VarByteArray labelArray, final VarByteArray imageArray) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				listener.getParent().setActiveComponent(new ImageViewer(labelArray, imageArray));
			}
		});
	}
	
	private void safeMessage(final String title, final String message, final int type) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane.showMessageDialog(listener.getParent(), message, title, type);
			}
		});
	}
	
}
