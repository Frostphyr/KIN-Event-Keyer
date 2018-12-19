package com.frostphyr.kin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.poi.ss.usermodel.Workbook;

import net.miginfocom.swing.MigLayout;

public class SheetSelectionDialog extends JDialog {

	private static final long serialVersionUID = -9045103741889625728L;
	
	private int index = -1;
	
	private JComboBox sheetComboBox;

	public SheetSelectionDialog(Workbook workbook) {
		super((Frame) null, "KIN Event Keyer", true);
		
		init(workbook);
	}
	
	private void init(Workbook workbook) {
		setLayout(new MigLayout());
		
		String[] sheets = new String[workbook.getNumberOfSheets()];
		for (int i = 0; i < sheets.length; i++) {
			sheets[i] = workbook.getSheetAt(i).getSheetName();
		}
		sheetComboBox = new JComboBox(sheets);
		add(sheetComboBox, "growx, wrap");
		
		JPanel buttonPanel = new JPanel();
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(confirmActionListener);
		buttonPanel.add(confirmButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancelActionListener);
		buttonPanel.add(cancelButton);
		add(buttonPanel, "dock center");
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public int getIndex() {
		return index;
	}
	
	private ActionListener confirmActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			index = sheetComboBox.getSelectedIndex();
			dispose();
		}
		
	};
	
	private ActionListener cancelActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
		
	};
	
}
