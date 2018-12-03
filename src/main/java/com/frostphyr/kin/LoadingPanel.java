package com.frostphyr.kin;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class LoadingPanel extends JPanel {

	private static final long serialVersionUID = -7431826198524720002L;
	
	public LoadingPanel() {
		super();
		
		init();
	}
	
	private void init() {
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Loading file");
		add(progressBar);
	}

}
