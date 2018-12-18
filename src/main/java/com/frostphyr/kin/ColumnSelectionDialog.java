package com.frostphyr.kin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.poi.ss.util.CellReference;

import net.miginfocom.swing.MigLayout;

public class ColumnSelectionDialog extends JDialog {

	private static final long serialVersionUID = 143194871948847741L;
	
	private EntryParser.Result parserResult;
	private Result result;
	
	private JComboBox departmentComboBox;
	private JComboBox categoryComboBox;
	private JComboBox percentComboBox;
	private JComboBox changeComboBox;
	
	public ColumnSelectionDialog(EntryParser.Result parserResult) {
		super((Frame) null, "KIN Event Keyer", true);
		
		this.parserResult = parserResult;
		
		init();
	}
	
	private void init() {
		setLayout(new MigLayout());
		departmentComboBox = new JComboBox(getColumnLetters(parserResult.getDivisionColumns()));
		add("Department", departmentComboBox);
		categoryComboBox = new JComboBox(getColumnLetters(parserResult.getCategoryColumns()));
		add("Category", categoryComboBox);
		percentComboBox = new JComboBox(getColumnLetters(parserResult.getPercentColumns()));
		add("Percent", percentComboBox);
		changeComboBox = new JComboBox(getColumnLetters(parserResult.getChangeColumns()));
		add("Change", changeComboBox);
		
		JPanel buttonPanel = new JPanel();
		JButton confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(confirmActionListener);
		buttonPanel.add(confirmButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(cancelActionListener);
		buttonPanel.add(cancelButton);
		add(buttonPanel, "span, dock center");
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	private void add(String name, JComboBox comboBox) {
		add(new JLabel(name + " Column: "));
		add(comboBox, "wrap");
	}
	
	private String[] getColumnLetters(List<Integer> columns) {
		String[] letters = new String[columns.size()];
		for (int i = 0; i < letters.length; i++) {
			letters[i] = CellReference.convertNumToColString(columns.get(i));
		}
		return letters;
	}
	
	public Result getResult() {
		return result;
	}
	
	private ActionListener confirmActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			result = new Result(parserResult.getDivisionColumns().get(departmentComboBox.getSelectedIndex()),
					parserResult.getCategoryColumns().get(categoryComboBox.getSelectedIndex()),
					parserResult.getPercentColumns().get(percentComboBox.getSelectedIndex()),
					parserResult.getChangeColumns().get(changeComboBox.getSelectedIndex()));
			dispose();
		}
		
	};
	
	private ActionListener cancelActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
		
	};
	
	public static class Result {
		
		private int divisionColumn;
		private int categoryColumn;
		private int percentColumn;
		private int changeColumn;
		
		private Result(int divisionColumn, int categoryColumn, int percentColumn, int changeColumn) {
			this.divisionColumn = divisionColumn;
			this.categoryColumn = categoryColumn;
			this.percentColumn = percentColumn;
			this.changeColumn = changeColumn;
		}
		
		public int getDivisionColumn() {
			return divisionColumn;
		}
		
		public int getCategoryColumn() {
			return categoryColumn;
		}
		
		public int getPercentColumn() {
			return percentColumn;
		}
		
		public int getChangeColumn() {
			return changeColumn;
		}
		
	}

}
