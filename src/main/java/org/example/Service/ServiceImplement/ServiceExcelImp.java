package org.example.Service.ServiceImplement;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.Exception.MessageException;
import org.example.Service.ServiceExcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Dynamic Excel export/import: any sheet name, any headers, any row data. */
public class ServiceExcelImp implements ServiceExcel {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public File writeToExcel(String filePath, String sheetName, List<String> headers, List<List<Object>> rows) throws MessageException {
        File file = prepareFile(filePath);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            writeHeader(workbook, sheet, headers);
            int rowNum = 1;
            for (List<Object> rowData : rows) {
                writeRow(sheet.createRow(rowNum++), rowData);
            }
            autoSize(sheet, headers.size());
            save(workbook, file);
            return file;
        } catch (IOException e) {
            throw new MessageException("Failed to write Excel file: " + e.getMessage());
        }
    }

    @Override
    public File appendToExcel(String filePath, String sheetName, List<String> headers, List<List<Object>> rows) throws MessageException {
        File file = prepareFile(filePath);
        try (Workbook workbook = openOrCreate(file)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                sheet = workbook.createSheet(sheetName);
                writeHeader(workbook, sheet, headers);
            }
            int rowNum = sheet.getLastRowNum() + 1;
            for (List<Object> rowData : rows) {
                writeRow(sheet.createRow(rowNum++), rowData);
            }
            autoSize(sheet, headers.size());
            save(workbook, file);
            return file;
        } catch (IOException e) {
            throw new MessageException("Failed to append to Excel file: " + e.getMessage());
        }
    }

    @Override
    public List<List<Object>> readFromExcel(String filePath, String sheetName) throws MessageException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new MessageException("Excel file not found: " + filePath);
        }
        List<List<Object>> result = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new MessageException("Sheet not found: " + sheetName);
            }
            for (Row row : sheet) {
                List<Object> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(readCellValue(cell));
                }
                result.add(rowData);
            }
        } catch (IOException e) {
            throw new MessageException("Failed to read Excel file: " + e.getMessage());
        }
        return result;
    }

    // ======================== HELPERS ========================

    private File prepareFile(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        return file;
    }

    private Workbook openOrCreate(File file) throws IOException {
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                return new XSSFWorkbook(fis);
            }
        }
        return new XSSFWorkbook();
    }

    private void save(Workbook workbook, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            workbook.write(fos);
        }
    }

    private void writeHeader(Workbook workbook, Sheet sheet, List<String> headers) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void writeRow(Row row, List<Object> rowData) {
        for (int i = 0; i < rowData.size(); i++) {
            writeCellValue(row.createCell(i), rowData.get(i));
        }
    }

    private void writeCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank();
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATE_FORMAT));
        } else if (value instanceof LocalDate) {
            cell.setCellValue(value.toString());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private Object readCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return cell.getStringCellValue();
        }
    }

    private void autoSize(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}