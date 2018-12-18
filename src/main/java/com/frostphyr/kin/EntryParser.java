package com.frostphyr.kin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EntryParser {
	
	public static Result parse(File file, String division, String category, String percent, String change) throws IOException, InvalidFormatException {
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			for (Row r : sheet) {
				List<Integer> divisionColumns = new ArrayList<Integer>();
				List<Integer> categoryColumns = new ArrayList<Integer>();
				List<Integer> percentColumns = new ArrayList<Integer>();
				List<Integer> changeColumns = new ArrayList<Integer>();
				for (Cell c : r) {
					if (c != null && c.getCellTypeEnum() == CellType.STRING) {
						String text = c.getStringCellValue();
						if (containsIgnoreCase(text, division)) {
							divisionColumns.add(c.getColumnIndex());
						} else if (containsIgnoreCase(text, category)) {
							categoryColumns.add(c.getColumnIndex());
						} else if (containsIgnoreCase(text, percent)) {
							percentColumns.add(c.getColumnIndex());
						} else if (containsIgnoreCase(text, change)) {
							changeColumns.add(c.getColumnIndex());
						}
					}
				}
				
				if (divisionColumns.size() == 1 && categoryColumns.size() == 1 && percentColumns.size() == 1 && changeColumns.size() == 1) {
					Result result = parse(sheet, divisionColumns.get(0), categoryColumns.get(0), percentColumns.get(0), changeColumns.get(0), r.getRowNum());
					workbook.close();
					return result;
				} else if (divisionColumns.size() >= 1 && categoryColumns.size() >= 1 && percentColumns.size() >= 1 && changeColumns.size() >= 1) {
					Result result = new Result(divisionColumns, categoryColumns, percentColumns, changeColumns, r.getRowNum());
					workbook.close();
					return result;
				} else {
					divisionColumns.clear();
					categoryColumns.clear();
					percentColumns.clear();
					changeColumns.clear();
				}
			}
			workbook.close();
			return new Result();
		} catch (IOException e) {
			if (workbook != null) {
				workbook.close();
			}
			throw e;
		}
	}
	
	public static Result parse(File file, ColumnInputDialog.Result result) throws InvalidFormatException, IOException {
		return parse(file, result.getDepartment(), result.getCategory(), result.getPercent(), result.getChange());
	}
	
	public static Result parse(File file, ColumnSelectionDialog.Result columns, int headerRow) throws InvalidFormatException, IOException {
		Workbook workbook = null;
		try {
			workbook = new XSSFWorkbook(file);
			Result result = parse(workbook.getSheetAt(0), columns.getDivisionColumn(), columns.getCategoryColumn(), columns.getPercentColumn(), columns.getChangeColumn(), headerRow);
			workbook.close();
			return result;
		} catch (IOException e) {
			if (workbook != null) {
				workbook.close();
			}
			throw e;
		}
	}
	
	public static Result parse(Sheet sheet, int divisionColumn, int categoryColumn, int percentColumn, int changeColumn, int headerRow) {
		List<Entry> entries = new ArrayList<Entry>();
		List<String[]> entryErrors = new ArrayList<String[]>();
		for (Row r : sheet) {
			if (headerRow == -1 || r.getRowNum() > headerRow) {
				Cell changeCell = r.getCell(changeColumn);
				if (changeCell != null && changeCell.getCellTypeEnum() == CellType.STRING && containsIgnoreCase(changeCell.getStringCellValue(), "Change")) {
					String division = getCellString(r.getCell(divisionColumn));
					if (division == null) {
						entryErrors.add(getColumns(r));
						continue;
					}
					
					String category = getCellString(r.getCell(categoryColumn));
					if (category == null) {
						entryErrors.add(getColumns(r));
						continue;
					}
					
					Cell percentCell = r.getCell(percentColumn);
					String percent;
					if (percentCell == null || percentCell.getCellTypeEnum() != CellType.NUMERIC) {
						entryErrors.add(getColumns(r));
						continue;
					} else {
						percent = Long.toString(Math.round((r.getCell(percentColumn).getNumericCellValue() * 100)));
					}
					
					entries.add(new Entry(division, category, percent));
				}
			}
		}
		return new Result(entries, entryErrors);
	}
	
	private static String getCellString(Cell cell) {
		if (cell == null || cell.getCellTypeEnum() != CellType.STRING) {
			return null;
		} else {
			String text = cell.getStringCellValue();
			int index = text.indexOf('.');
			if (index != -1) {
				text = text.substring(0, index).trim();
				for (int i = 0; i < text.length(); i++) {
					if (!Character.isDigit(text.charAt(i))) {
						return null;
					}
				}
				return text;
			}
			return null;
		}
	}
	
	private static boolean containsIgnoreCase(String s1, String s2) {
		return Pattern.compile(Pattern.quote(s2), Pattern.CASE_INSENSITIVE).matcher(s1).find();
	}
	
	private static String[] getColumns(Row row) {
		List<String> columns = new ArrayList<String>();
		for (Cell c : row) {
			if (c == null) {
				columns.add("");
			} else {
				switch (c.getCellTypeEnum()) {
					case _NONE:
					case BLANK:
						columns.add("");
						break;
					case NUMERIC:
						columns.add(Double.toString(c.getNumericCellValue()));
						break;
					case STRING:
						columns.add(c.getStringCellValue());
						break;
					case BOOLEAN:
						columns.add(Boolean.toString(c.getBooleanCellValue()));
						break;
					case FORMULA:
						columns.add(c.getCellFormula());
						break;
					case ERROR:
						columns.add(Byte.toString(c.getErrorCellValue()));
						break;
				}
			}
		}
		return columns.toArray(new String[columns.size()]);
	}
	
	public static class Result {
		
		public static final int SUCCESS = 0;
		public static final int DUPLICATE_COLUMNS = 1;
		public static final int INVALID_COLUMNS = 2;
		
		private int type;
		
		private List<Entry> entries;
		private List<String[]> entryErrors;
		
		private List<Integer> divisionColumns;
		private List<Integer> categoryColumns;
		private List<Integer> percentColumns;
		private List<Integer> changeColumns;
		private int headerRow;
		
		private Result(List<Entry> entries, List<String[]> entryErrors) {
			this.type = SUCCESS;
			this.entries = entries;
			this.entryErrors = entryErrors;
		}
		
		private Result(List<Integer> divisionColumns, List<Integer> categoryColumns, List<Integer> percentColumns, List<Integer> changeColumns, int headerRow) {
			this.type = DUPLICATE_COLUMNS;
			this.divisionColumns = divisionColumns;
			this.categoryColumns = categoryColumns;
			this.percentColumns = percentColumns;
			this.changeColumns = changeColumns;
			this.headerRow = headerRow;
		}
		
		private Result() {
			type = INVALID_COLUMNS;
		}
		
		public int getType() {
			return type;
		}
		
		public List<Entry> getEntries() {
			return entries;
		}
		
		public List<String[]> getEntryErrors() {
			return entryErrors;
		}
		
		public List<Integer> getDivisionColumns() {
			return divisionColumns;
		}
		
		public List<Integer> getCategoryColumns() {
			return categoryColumns;
		}
		
		public List<Integer> getPercentColumns() {
			return percentColumns;
		}
		
		public List<Integer> getChangeColumns() {
			return changeColumns;
		}
		
		public int getHeaderRow() {
			return headerRow;
		}
		
	}

}
