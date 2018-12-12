package com.frostphyr.kin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

public class Launcher {

	public static void main(String[] args) {
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);
		try {
			GlobalScreen.registerNativeHook();
			
		} catch (NativeHookException e) {
		}
		
		try {
			UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
		} catch (Exception e) {
		}
		
		File eventFile = null;
		File file = new File("event.xlsx");
		if (file.exists()) {
			eventFile = file;
		} else {
			JFileChooser chooser = new JFileChooser();
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File file) {
					if (file.isDirectory()) {
						return true;
					}
					
					String extension = getExtension(file);
					return extension != null && extension.equalsIgnoreCase("xlsx");
				}

				@Override
				public String getDescription() {
					return "XLSX File";
				}
				
			});
			int option = chooser.showOpenDialog(null);
			if (option == JFileChooser.APPROVE_OPTION) {
				eventFile = chooser.getSelectedFile();
			} else {
				try {
					GlobalScreen.unregisterNativeHook();
				} catch (NativeHookException e) {
				}
			}
		}
		
		if (eventFile != null) {
			final JFrame frame = new JFrame("KIN Event Keyer");
			final JPanel loadingPanel = new LoadingPanel();
			showLoadingPanel(frame, loadingPanel);
			
			try {
				EntryParser.Result result = EntryParser.parse(eventFile, "Division", "Category", "Regular Type", "Change");
				while (result.getType() != EntryParser.Result.SUCCESS) {
					hideFrame(frame);
					
					if (result.getType() == EntryParser.Result.DUPLICATE_COLUMNS) {
						ColumnSelectionDialog.Result columnResult = new ColumnSelectionDialog(result).getResult();
						if (columnResult == null) {
							exit(frame);
							return;
						}
						
						showLoadingPanel(frame, loadingPanel);
						result = EntryParser.parse(eventFile, columnResult, result.getHeaderRow());
					} else {
						ColumnInputDialog.Result columnResult = new ColumnInputDialog().getResult();
						if (columnResult == null) {
							exit(frame);
							return;
						}
						
						showLoadingPanel(frame, loadingPanel);
						result = EntryParser.parse(eventFile, columnResult);
					}
				}
				
				if (result.getEntries().size() > 0) {
					showMainPanel(frame, loadingPanel, result);
				} else {
					fatalError(frame, "No entries found");
				}
			} catch (InvalidFormatException e) {
				fatalError(frame, "Invalid file format");
			} catch (IOException e) {
				fatalError(frame, e.getMessage());
			}
		}
	}
	
	private static void showLoadingPanel(final JFrame frame, final JPanel loadingPanel) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.add(loadingPanel);
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
			
		});
	}
	
	private static void hideFrame(final JFrame frame) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame.setVisible(false);
			}
			
		});
	}
	
	private static void showMainPanel(final JFrame frame, final JPanel loadingPanel, final EntryParser.Result result) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				frame.remove(loadingPanel);
				frame.add(new MainPanel(result));
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.repaint();
			}
			
		});
	}
	
	private static void exit(JFrame frame) {
		frame.dispose();
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException ex) {
		}
	}
	
	private static void fatalError(JFrame frame, String message) {
		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.PLAIN_MESSAGE);
		exit(frame);
	}
	
	private static String getExtension(File file) {
		String ext = null;
		String s = file.getName();
		int i = s.lastIndexOf('.');
		
		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

}
