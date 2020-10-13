package uk.ac.bbsrc.tgac.miso.core.data.spreadsheet;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Open a file containing an Excel file previously generated by MISO (1-row header) and emit a stream of rows as strings.
 */
public class ExcelReader extends SpreadSheetReader<XSSFSheet, XSSFRow> {

  public static Stream<String[]> open(InputStream stream) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook(stream);
    return SpreadSheetReader.createStream(workbook.getSheetAt(0), new ExcelReader());
  }

  @Override
  protected String getColumn(XSSFSheet sheet, XSSFRow row, int i) {
    return row.getCell(i).getStringCellValue();
  }

  @Override
  protected int getColumns(XSSFSheet sheet, XSSFRow row) {
    return row.getLastCellNum();
  }

  @Override
  protected XSSFRow getRow(XSSFSheet sheet, int index) {
    return sheet.getRow(index);
  }

  @Override
  protected int getRows(XSSFSheet sheet) {
    return sheet.getLastRowNum();
  }
}
