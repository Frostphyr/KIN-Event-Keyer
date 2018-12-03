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
				int divisionColumn = -1;
				int categoryColumn = -1;
				int percentColumn = -1;
				int changeColumn = -1;
				for (Cell c : r) {
					if (c != null && c.getCellTypeEnum() == CellType.STRING) {
						String text = c.getStringCellValue();
						if (containsIgnoreCase(text, division)) {
							divisionColumn = c.getColumnIndex();
						} else if (containsIgnoreCase(text, category)) {
							categoryColumn = c.getColumnIndex();
						} else if (containsIgnoreCase(text, percent)) {
							percentColumn = c.getColumnIndex();
						} else if (containsIgnoreCase(text, change)) {
							changeColumn = c.getColumnIndex();
						}
					}
				}
				
				if (divisionColumn != -1 && categoryColumn != -1 && percentColumn != -1 && changeColumn != -1) {
					Result result = parse(sheet, divisionColumn, categoryColumn, percentColumn, changeColumn, r.getRowNum());
					workbook.close();
					return result;
				} else {
					divisionColumn = -1;
					categoryColumn = -1;
					percentColumn = -1;
				}
			}
			workbook.close();
			return null;
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
	
	private static Result parse(Sheet sheet, int divisionColumn, int categoryColumn, int percentColumn, int changeColumn, int headerRow) {
		List<Entry> entries = new ArrayList<Entry>();
		List<String[]> entryErrors = new ArrayList<String[]>();
		for (Row r : sheet) {
			if (headerRow == -1 || r.getRowNum() != headerRow) {
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
						percent = Integer.toString((int) (r.getCell(percentColumn).getNumericCellValue() * 100));
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
		
		private List<Entry> entries;
		private List<String[]> entryErrors;
		
		private Result(List<Entry> entries, List<String[]> entryErrors) {
			this.entries = entries;
			this.entryErrors = entryErrors;
		}
		
		public List<Entry> getEntries() {
			return entries;
		}
		
		public List<String[]> getEntryErrors() {
			return entryErrors;
		}
		
	}

}
