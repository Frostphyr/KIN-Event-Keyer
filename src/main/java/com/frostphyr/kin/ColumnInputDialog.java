package com.frostphyr.kin;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class ColumnInputDialog extends JDialog {

	private static final long serialVersionUID = 3612441935382598341L;
	
	private JTextField departmentTextField = new JTextField(10);
	private JTextField categoryTextField = new JTextField(10);
	private JTextField percentTextField = new JTextField(10);
	private JTextField changeTextField = new JTextField(10);
	
	private Result result;
	
	public ColumnInputDialog() {
		super((Frame) null, true);
		
		init();
	}
	
	private void init() {
		setTitle("KIN Event Keyer");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLayout(new MigLayout());
		
		add(new JLabel("Department: "));
		add(departmentTextField, "wrap");
		add(new JLabel("Category: "));
		add(categoryTextField, "wrap");
		add(new JLabel("Percent: "));
		add(percentTextField, "wrap");
		add(new JLabel("Change: "));
		add(changeTextField, "wrap");
		
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
	
	public Result getResult() {
		return result;
	}
	
	private String validate(JTextField textField, String name) {
		String text = textField.getText().trim();
		if (text.equals("")) {
			JOptionPane.showMessageDialog(this, "Please enter the " + name + " column name", "Error", JOptionPane.PLAIN_MESSAGE);
			return null;
		} else {
			return text;
		}
	}
	
	private ActionListener confirmActionListener = new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent e) {
			String department = validate(departmentTextField, "department");
			if (department == null) {
				return;
			}
			
			String category = validate(categoryTextField, "category");
			if (category == null) {
				return;
			}
			
			String percent = validate(percentTextField, "percent");
			if (percent == null) {
				return;
			}
			
			String change = validate(changeTextField, "change");
			if (change == null) {
				return;
			}
			
			result = new Result(department, category, percent, change);
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
		
		private String department;
		private String category;
		private String percent;
		private String change;
		
		private Result(String department, String category, String percent, String change) {
			this.department = department;
			this.category = category;;
			this.percent = percent;
			this.change = change;
		}

		public String getDepartment() {
			return department;
		}

		public String getCategory() {
			return category;
		}
		
		public String getPercent() {
			return percent;
		}
		
		public String getChange() {
			return change;
		}
		
	}

}
