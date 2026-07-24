package org.example.Service;

import org.example.Exception.MessageException;

import java.io.File;
import java.util.List;

/** Generic Excel read/write service — works with any headers + row data, not tied to one entity. */
public interface ServiceExcel {

    /** Creates (or overwrites) a workbook with a single sheet containing the given headers and rows. */
    File writeToExcel(String filePath, String sheetName, List<String> headers, List<List<Object>> rows) throws MessageException;

    /** Appends rows to a sheet, creating the file/sheet/header row on first use. */
    File appendToExcel(String filePath, String sheetName, List<String> headers, List<List<Object>> rows) throws MessageException;

    /** Reads a sheet back as raw rows (first row is the header row). */
    List<List<Object>> readFromExcel(String filePath, String sheetName) throws MessageException;
}