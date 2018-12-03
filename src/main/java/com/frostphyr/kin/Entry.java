package com.frostphyr.kin;

public class Entry {
	
	private String division;
	private String category;
	private String percent;
	
	public Entry(String division, String category, String percent) {
		this.division = division;
		this.category = category;
		this.percent = percent;
	}
	
	public String getDivision() {
		return division;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getPercent() {
		return percent;
	}

}
