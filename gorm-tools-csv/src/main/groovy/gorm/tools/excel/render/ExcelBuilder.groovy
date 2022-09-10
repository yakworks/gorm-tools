/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.excel.render

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

import groovy.transform.CompileStatic

import org.apache.poi.ss.usermodel.BorderStyle
import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.DataFormat
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import yakworks.commons.lang.PropertyTools
import yakworks.commons.map.MapFlattener
import yakworks.meta.MetaMapList

@SuppressWarnings(['NestedBlockDepth'])
@CompileStatic
class ExcelBuilder {
    public static final String SHEET_NAME = "data"
    public static final DefaultIndexedColorMap INDEXED_COLOR_MAP = new DefaultIndexedColorMap()

    XSSFWorkbook workbook
    XSSFSheet sheet
    CreationHelper createHelper

    Set<String> headers
    OutputStream outputStream

    XSSFCellStyle styleHeader
    XSSFCellStyle styleMoney
    XSSFCellStyle styleLocalDate
    XSSFCellStyle styleDateTime

    /**
     * builder helper to create with an outputStream
     */
    static ExcelBuilder of(OutputStream outstream){
        def eb = new ExcelBuilder()
        eb.outputStream = outstream
        return eb
    }

    /**
     * creates the workbook and a sheet with default styles that can be used
     */
    ExcelBuilder build(){
        workbook = new XSSFWorkbook()
        sheet = workbook.createSheet(SHEET_NAME)
        createHelper = sheet.workbook.getCreationHelper()
        initDefaultStyles()
        return this
    }

    /**
     * initializes the default styles
     */
    void initDefaultStyles(){
        //NUMBER MONEY
        styleMoney = workbook.createCellStyle()
        styleMoney.alignment = HorizontalAlignment.RIGHT
        // cellStyle.setFont(font)
        styleMoney.wrapText = true
        DataFormat xformat = workbook.createDataFormat()
        styleMoney.setDataFormat(xformat.getFormat("#,##0.00"))

        //DATE
        styleLocalDate = workbook.createCellStyle()
        // DataFormat ldformat = sheet.workbook.createDataFormat()
        // localDateStyle.setDataFormat(ldformat.getFormat("mm/dd/yyyy"))
        styleLocalDate.setDataFormat((short)BuiltinFormats.getBuiltinFormat("m/d/yy"))

        styleDateTime = workbook.createCellStyle()
        styleDateTime.setDataFormat((short)BuiltinFormats.getBuiltinFormat("m/d/yy h:mm"))

        //HEADERS Style
        styleHeader = workbook.createCellStyle()
        // headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex())
        styleHeader.setFillForegroundColor(parseColor("#F5F5F5"))
        styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        styleHeader.setBorderBottom(BorderStyle.MEDIUM)
        styleHeader.dataFormatString
        XSSFFont font = workbook.createFont()
        // font.setFontName("Calibri")
        font.setFontHeightInPoints((short) 12)
        font.setBold(true)
        styleHeader.setFont(font)
    }


    /**
     * Writes the dataList a (collection of maps) to the active sheet.
     * If dataList is a MetaMapList then it will use its metaEntity to get the headers.
     * otherwise it creates the header using the the data in the first item of collection.
     *
     * @param dataList a List of Maps or a MetaMapList
     */
    void write(Collection<Map> dataList){
        // BenchmarkHelper.startTime()
        createHeader(dataList)

        int rowIdx = 1 //start at 1 as row 0 is header
        dataList.eachWithIndex{ Map rowData, int i ->
            List vals = headers.collect{ PropertyTools.getProperty(rowData, it) }
            Row row = createRow(rowIdx + i, vals)
        }
    }

    /**
     * If dataList is a MetaMapList then it will use its metaEntity to get the headers.
     * otherwise it creates the header using the the data in the first item of collection.
     */
    void createHeader(Collection<Map> dataList){
        if(dataList instanceof MetaMapList){
            headers = dataList.metaEntity.flattenProps()
            writeHeader(headers)
        } else {
            Map<String, Object> firstRow = dataList[0] as Map<String, Object>
            Map flatRow = MapFlattener.of(firstRow).convertObjectToString(false).flatten()
            headers = flatRow.keySet()
            writeHeader(headers)
        }
    }

    /**
     * Creates the row from a data list.
     * @param rowIndex the row number, starts at 0
     * @param rowData the collection of datum
     * @return the created row, returns for testing, dont need to do anything with it
     */
    Row createRow(int rowIndex, Collection rowData) {
        Row row = sheet.createRow(rowIndex)
        rowData.eachWithIndex{ Object field, int i ->
            createCell(row, i, field)
        }
        return row
    }

    /**
     * creates a cell in the row at the column index (starts at zero).
     * one of the createRow should have been called first
     * @param row the row to create the cell in.
     * @param columnIndex the col number
     * @param value the value
     * @return the cell, returns for testing, dont need to do anything with it
     *
     * @see #createRow
     */
    Cell createCell(Row row, int columnIndex, Object value) {
        Cell cell = row.createCell(columnIndex)
        setCellValue(cell, value)
        return cell
    }

    /**
     * sets the excel cell value by first doing some conversion from java types to excel types
     * @param cell the cell to set the value on
     * @param value the value
     * @return returns mostly for testing, dont need to do anything with it
     */
    Cell setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setBlank()
            return cell
        }

        if (value instanceof BigDecimal) {
            cell.setCellValue(((Number) value).doubleValue())
            cell.setCellStyle(styleMoney)
            return cell
        }

        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue())
            return cell
        }

        if(value instanceof LocalDate ){
            cell.setCellValue(value)
            cell.setCellStyle(styleLocalDate)
            return cell
        }

        if(value instanceof LocalDateTime ){
            cell.setCellValue(value.truncatedTo(ChronoUnit.SECONDS))
            cell.setCellStyle(styleDateTime)
            return cell
        }

        if(value instanceof Date){
            cell.setCellValue(value)
            cell.setCellStyle(styleDateTime)
            return cell
        }

        if (value instanceof Calendar) {
            cell.setCellValue((Calendar) value)
            return cell
        }

        if (value instanceof Boolean) {
            cell.setCellValue((value ? 1 : 0).doubleValue())
            return cell
        }

        cell.setCellValue(value.toString())
        return cell
    }

    /**
     * Write out header row at index 0 using the header style.
     */
    void writeHeader(Collection<String> titles) {
        XSSFRow header = sheet.createRow(0)

        titles.eachWithIndex{ String title, int i ->
            Cell headerCell = header.createCell(i)
            headerCell.setCellValue(title)
            headerCell.setCellStyle(styleHeader)
        }
    }

    /**
     * userful for testing to write out and close stream
     */
    void writeAndClose() {
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Parse a normal hex color like "#FFFFFF" into poi XSSFColor that needs byte array for color
     */
    static XSSFColor parseColor(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("Please, provide the color in '#abcdef' hex string format");
        }


        Matcher match = Pattern.compile("#([\\dA-F]{2})([\\dA-F]{2})([\\dA-F]{2})").matcher(hex.toUpperCase());

        if (!match.matches()) {
            throw new IllegalArgumentException("Cannot parse color " + hex + ". Please, provide the color in \'#abcdef\' hex string format");
        }


        byte red = (byte) Integer.parseInt(match.group(1), 16)
        byte green = (byte) Integer.parseInt(match.group(2), 16)
        byte blue = (byte) Integer.parseInt(match.group(3), 16)

        return new XSSFColor(new byte[]{red, green, blue}, INDEXED_COLOR_MAP)
    }

}
