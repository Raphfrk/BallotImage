package com.raphfrk.ballotimage.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Hello world!
 *
 */
public class GUIMain extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final JLabel statusBar;
	private final LinkedList<String> statusStrings = new LinkedList<String>();
	private Component activeComp = null;

	public static void main( String[] args ) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new GUIMain().setVisible(true);
			}
		});
    }
	
	public GUIMain() {
		
		ActionListener listener = new GUIMainActionListener(this);
		
		JMenuBar menuBar = new JMenuBar();
		
        menuBar.add(createJMenu(listener, "File", "Open", "Exit"));
        
        setJMenuBar(menuBar);
        
        statusBar = new JLabel();
        statusBar.setPreferredSize(new Dimension(24, 24));
        
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        
        setTitle("BallotImage");
        setSize(600, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
	}
	
	public void setStatusBar(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				statusStrings.addFirst(text);
				refreshStatusBar();
			}
		});
	}
	
	public void unsetStatusBar(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Iterator<String> i = statusStrings.iterator();
				while (i.hasNext()) {
					if (text.equals(i.next())) {
						i.remove();
						break;
					}
				}
				refreshStatusBar();
			}
		});
	}
	
	private void refreshStatusBar() {
		int size = statusStrings.size();
		if (size == 0) {
			statusBar.setText(" ");
		} else {
			statusBar.setText(statusStrings.getFirst());
		}
	}
	
	public void setActiveComponent(final Component comp) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (activeComp != null) {
					getContentPane().remove(activeComp);
					if (activeComp instanceof Closeable) {
						try {
							((Closeable) activeComp).close();
						} catch (IOException ioe) {
						}
					}
				}
				activeComp = comp;
				if (comp != null) {
					getContentPane().add(comp, BorderLayout.CENTER);
				}
				getContentPane().revalidate();
			}
		});
	}
	
	private JMenu createJMenu(ActionListener listener, String name, String... entries) {
		JMenu menu = new JMenu(name);
		for (String entry : entries) {
			JMenuItem menuEntry = new JMenuItem(entry);
			menuEntry.setActionCommand(name + "-" + entry);
			menuEntry.addActionListener(listener);
			menu.add(menuEntry);
		}
		return menu;
	}

}
