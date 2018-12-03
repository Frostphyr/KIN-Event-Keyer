package com.frostphyr.kin;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import net.miginfocom.swing.MigLayout;

public class MainPanel extends JPanel {

	private static final long serialVersionUID = 118780168388221726L;
	
	private JLabel departmentLabel = new JLabel();
	private JLabel categoryLabel = new JLabel();
	private JLabel percentLabel = new JLabel();
	
	private JButton previousButton = new JButton("<");
	private JButton nextButton = new JButton(">");
	private JFormattedTextField currentIndexTextField = new JFormattedTextField();
	private JLabel sizeLabel = new JLabel();
	
	private JFormattedTextField initialDelayTextField = new JFormattedTextField();
	private JFormattedTextField keyDelayTextField = new JFormattedTextField();
	private JCheckBox reminderCheckBox = new JCheckBox("Online order reminder");
	
	private JButton startButton = new JButton("Start");
	private JButton stopButton = new JButton("Stop");
	
	private EntryParser.Result result;
	private EntryKeyer keyer;
	
	public MainPanel(EntryParser.Result result) {
		super(new MigLayout());
		
		this.result = result;
		init();
	}
	
	private void init() {
		if (result.getEntryErrors().size() > 0) {
			DefaultTableModel model = new DefaultTableModel() {

				private static final long serialVersionUID = 3178665237666715799L;
				
				@Override
				public boolean isCellEditable(int row, int column) {
					return false;
				}
				
			};
			
			for (String[] s : result.getEntryErrors()) {
				while (model.getColumnCount() < s.length) {
					model.addColumn(null);
				}
				model.addRow(s);
			}
			
			JTable table = new JTable(model);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scrollPane = new JScrollPane(table);
			scrollPane.setPreferredSize(new Dimension(table.getPreferredSize().width, table.getRowHeight() * 3));
			table.setFillsViewportHeight(true);
			add(scrollPane, "span, grow, push, wrap");
			add(new JSeparator(SwingConstants.HORIZONTAL), "span, growx, wrap");
		}
		
		add(new JLabel("Department"), "pushx");
		add(new JSeparator(SwingConstants.VERTICAL), "spany 3, growy");
		add(new JLabel("Category"), "pushx");
		add(new JSeparator(SwingConstants.VERTICAL), "spany 3, growy");
		add(new JLabel("Percent"), "pushx, wrap");
		add(new JSeparator(SwingConstants.HORIZONTAL), "span, growx, wrap");
		add(departmentLabel, "pushx");
		add(categoryLabel, "pushx");
		add(percentLabel, "pushx, wrap");
		
		currentIndexTextField.setColumns(4);
		((AbstractDocument) currentIndexTextField.getDocument()).setDocumentFilter(currentIndexDocumentFilter);
		sizeLabel.setText("/" + result.getEntries().size());
		previousButton.addActionListener(new NavigationActionListener(-2));
		nextButton.addActionListener(new NavigationActionListener(0));
		JPanel countPanel = new JPanel();
		countPanel.add(previousButton);
		countPanel.add(currentIndexTextField);
		countPanel.add(sizeLabel);
		countPanel.add(nextButton);
		add(countPanel, "span, center, wrap");
		add(new JSeparator(SwingConstants.HORIZONTAL), "span, growx, wrap");
		
		initialDelayTextField.setColumns(4);
		keyDelayTextField.setColumns(4);
		((AbstractDocument) initialDelayTextField.getDocument()).setDocumentFilter(numericDocumentFilter);
		((AbstractDocument) keyDelayTextField.getDocument()).setDocumentFilter(numericDocumentFilter);
		add(new JLabel("Initial delay (ms)"), "span, split 2");
		add(initialDelayTextField, "pushx, growx, wrap");
		add(new JLabel("Key delay (ms)"), "span, split 2");
		add(keyDelayTextField, "pushx, growx, wrap");
		add(reminderCheckBox, "span, wrap");
		
		startButton.addActionListener(startActionListener);
		stopButton.addActionListener(stopActionListener);
		stopButton.setEnabled(false);
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(startButton);
		buttonPanel.add(stopButton);
		add(buttonPanel, "span, dock center");
		
		setCurrentEntry(0, true);
		
		GlobalScreen.addNativeKeyListener(nativeKeyListener);
	}
	
	private void setCurrentEntry(int index, boolean updateCurrentIndex) {
		Entry entry = result.getEntries().get(index);
		departmentLabel.setText(entry.getDivision());
		categoryLabel.setText(entry.getCategory());
		percentLabel.setText(entry.getPercent() + '%');
		if (updateCurrentIndex) {
			currentIndexTextField.setText(Integer.toString(index + 1));
		}
		
		previousButton.setEnabled(true);
		nextButton.setEnabled(true);
		if (index == 0) {
			previousButton.setEnabled(false);
		} else if (index >= result.getEntries().size() - 1) {
			nextButton.setEnabled(false);
		}
	}
	
	private int getCurrentIndex() {
		String text = currentIndexTextField.getText();
		if (text.equals("")) {
			return 0;
		}
		
		
		try {
			int index = Integer.parseInt(text);
			if (index >= 0 && index < result.getEntries().size()) {
				return index;
			}
		} catch (NumberFormatException ex) {
		}
		setCurrentEntry(0, true);
		return 0;
	}
	
	private void showErrorMessage(String text) {
		JOptionPane.showMessageDialog(null, text, "Error", JOptionPane.PLAIN_MESSAGE);
	}
	
	private void start() {
		if (keyer != null) {
			keyer.shutdown();
			keyer = null;
		}
		
		int initialDelay = validateDelay(initialDelayTextField, "Initial");
		if (initialDelay == -1) {
			return;
		}
		
		int keyDelay = validateDelay(keyDelayTextField, "Key");
		if (keyDelay == -1) {
			return;
		}
		
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
		
		try {
			final int index = getCurrentIndex();
			final long startTime = System.currentTimeMillis();
			keyer = new EntryKeyer(result.getEntries(), initialDelay, keyDelay, reminderCheckBox.isSelected(), new EntryKeyer.Callback() {

				@Override
				public void onNextEntry(Entry entry, final int index) {
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							setCurrentEntry(index, true);
						}
						
					});
				}

				@Override
				public void onFinish() {
					long ms = System.currentTimeMillis() - startTime;
					long s = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;
					long m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
					long h = TimeUnit.MILLISECONDS.toHours(ms) % 24;
					JOptionPane.showMessageDialog(null, (result.getEntries().size() - index) + " entries keyed in " + String.format("%d:%02d:%02d", h,m,s));
				}
				
			});
			keyer.start(index);
		} catch (AWTException ex) {
			showErrorMessage(ex.getMessage());
		}
	}
	
	private int validateDelay(JFormattedTextField textField, String name) {
		try {
			int delay = Integer.parseInt(textField.getText());
			if (delay < 0) {
				showErrorMessage(name + " delay must be greater than or equal to 0");
				return -1;
			} else {
				return delay;
			}
		} catch (NumberFormatException ex) {
			showErrorMessage("Invalid " + name.toLowerCase() + " delay");
			return -1;
		}
	}
	
	private void stop() {
		if (keyer != null) {
			keyer.shutdown();
			keyer = null;
		}
		
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}
	
	private DocumentFilter currentIndexDocumentFilter = new DocumentFilter() {
		
		@Override
		public void remove(FilterBypass fb, int offset, int length) {
			try {
				String text = new StringBuilder(getText(fb))
						.replace(offset, offset + length, "")
						.toString();
				
				if (validate(text)) {
					super.remove(fb, offset, length);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attrs) {
			try {
				String text = new StringBuilder(getText(fb))
						.insert(offset, string)
						.toString();
				
				if (validate(text)) {
					super.insertString(fb, offset, string, attrs);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) {
			try {
				String text = new StringBuilder(getText(fb))
						.replace(offset, offset + length, string)
						.toString();
				
				if (validate(text)) {
					super.replace(fb, offset, length, string, attrs);
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		
		private String getText(FilterBypass fb) throws BadLocationException {
			Document doc = fb.getDocument();
			return doc.getText(0, doc.getLength());
		}
		
		private boolean validate(String text) {
			int index = -1;
			if (text.equals("")) {
				index = 1;
			} else {
				try {
					int i = Integer.parseInt(text);
					if (i >= 1 && i <= result.getEntries().size()) {
						index = i;
					}
				} catch (NumberFormatException e) {
				}
			}
			
			if (index != -1) {
				setCurrentEntry(index - 1, false);
				return true;
			}
			return false;
		}
		
	};
	
	private DocumentFilter numericDocumentFilter = new DocumentFilter() {
		
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attrs) {
			if (isNumeric(string)) {
				try {
					super.insertString(fb, offset, string, attrs);
				} catch (BadLocationException e) {
				}
			}
		}
		
		@Override
		public void replace(FilterBypass fb, int offset, int length, String string, AttributeSet attrs) {
			if (isNumeric(string)) {
				try {
					super.replace(fb, offset, length, string, attrs);
				} catch (BadLocationException e) {
				}
			}
		}
		
		private boolean isNumeric(String string) {
			for (int i = 0; i < string.length(); i++) {
				if (!Character.isDigit(string.charAt(i))) {
					return false;
				}
			}
			return true;
		}
		
	};
	
	private ActionListener startActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			start();
		}
		
	};
	
	private ActionListener stopActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			stop();
		}
		
	};
	
	private NativeKeyListener nativeKeyListener = new NativeKeyListener() {

		@Override
		public void nativeKeyTyped(NativeKeyEvent e) {
		}

		@Override
		public void nativeKeyPressed(NativeKeyEvent e) {
			if (e.getKeyCode() == NativeKeyEvent.VC_F11) {
				if (keyer != null) {
					stop();
				} else {
					start();
				}
			}
		}

		@Override
		public void nativeKeyReleased(NativeKeyEvent e) {
		}
		
	};
	
	private class NavigationActionListener implements ActionListener {
		
		private int delta;
		
		public NavigationActionListener(int delta) {
			this.delta = delta;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				int index = getCurrentIndex() + delta;
				if (index >= 0 && index < result.getEntries().size()) {
					setCurrentEntry(index, true);
				}
			} catch (NumberFormatException ex) {
				setCurrentEntry(0, true);
			}
		}
		
	};

}
