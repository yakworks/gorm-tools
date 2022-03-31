/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.excel.render

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.Temporal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import builders.dsl.spreadsheet.builder.poi.PoiSpreadsheetBuilder
import com.opencsv.CSVWriter
import yakworks.commons.lang.DateUtil
import yakworks.commons.map.MapFlattener

@CompileStatic
class XlsxMapWriter {
    CSVWriter csvWriter
    Set<String> headers
    OutputStream outputStream
    public static final String SHEET_NAME = "data"

    XlsxMapWriter(OutputStream outstream){
        this.outputStream = outstream
    }

    static XlsxMapWriter of(OutputStream outstream){
        return new XlsxMapWriter(outstream)
    }

    // void flush(){
    //     csvWriter.flush()
    // }

    void writeXlsx(List<Map> dataList){
        //flatten
        Map<String, Object> firstRow = dataList[0] as Map<String, Object>
        Map flatRow = flattenMap(firstRow)
        headers = flatRow.keySet()

        PoiSpreadsheetBuilder.create(outputStream).build {
            apply BookExcelStylesheet
            sheet(SHEET_NAME) { s ->
                row {
                    headers.each { header ->
                        cell {
                            value header
                            style BookExcelStylesheet.STYLE_HEADER
                        }
                    }
                }
                dataList.eachWithIndex{ rowData, int i->
                    //get all the values for the masterHeaders keys
                    def flatData = flattenMap(rowData as Map)
                    def vals = headers.collect{ flatData[it]}

                    row {
                        vals.each { dta ->
                            cell {
                                if(dta instanceof BigDecimal){
                                    value dta
                                    style {
                                        format "#,##0.00"
                                    }
                                } else if(dta instanceof LocalDate || dta instanceof LocalDateTime || dta instanceof Date){
                                    value DateUtil.convertToDate(dta)
                                    style {
                                        format "dd/mm/yyyy"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Map flattenMap(Map map){
        MapFlattener.of(map).convertObjectToString(false).flatten()
    }

    /**
     * WIP example of directly using poi instead of builder
     */
    void writeHeadersPoi(Map<String, Object> firstItem){
        headers = firstItem.keySet()
        Workbook workbook = new XSSFWorkbook()

        XSSFSheet sheet = workbook.createSheet("Data")
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 4000)

        XSSFRow header = sheet.createRow(0)

        XSSFCellStyle headerStyle = workbook.createCellStyle()
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex())
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)

        // XSSFFont font = ((XSSFWorkbook) workbook).createFont()
        // font.setFontName("Arial");
        // font.setFontHeightInPoints((short) 16);
        // font.setBold(true);
        // headerStyle.setFont(font);
        headers.each { it ->
            Cell headerCell = header.createCell(0)
            headerCell.setCellValue(it)
            headerCell.setCellStyle(headerStyle)
        }
    }


}
