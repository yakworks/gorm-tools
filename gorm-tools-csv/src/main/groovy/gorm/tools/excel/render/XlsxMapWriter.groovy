/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.excel.render

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

import com.opencsv.CSVWriter
import yakworks.commons.map.MapFlattener

@CompileStatic
class XlsxMapWriter {
    CSVWriter csvWriter
    Set<String> headers
    OutputStream outputStream

    XlsxMapWriter(OutputStream outstream){
        this.outputStream = outstream
    }

    static XlsxMapWriter of(OutputStream outstream){
        return new XlsxMapWriter(outstream)
    }

    // void flush(){
    //     csvWriter.flush()
    // }

    void writeHeaders(Map<String, Object> firstItem){
        //filter out the key that has the detail
        headers = firstItem.keySet()
        writeNext(csvWriter, headers)
    }

    void writeXlsx(List<Map> dataList){

        //flatten
        Map<String, Object> firstRow = dataList[0] as Map<String, Object>
        Map flatRow = flattenMap(firstRow)
        writeHeaders(flatRow)

        dataList.eachWithIndex{ row, int i->
            //get all the values for the masterHeaders keys
            writeLine(flattenMap(row as Map))

            //flush every 1000
            if (i % 1000 == 0) {
                csvWriter.flush()
            }
        }
    }

    Map flattenMap(Map map){
        MapFlattener.of(map).convertObjectToString(true).flatten()
    }

    void writeLine(Map data){
        def vals = headers.collect{ data[it] as String}
        writeNext(csvWriter, vals)
    }

    // for some reason compileStatic fails trying to call this method
    @CompileDynamic
    static void writeAll(CSVWriter writer, Object lines){
        writer.writeAll(lines)
    }

    @CompileDynamic
    static void writeNext(CSVWriter writer, Collection vals){
        writer.writeNext(vals as String[])
    }

    void writeXlsxHeaders(Map<String, Object> firstItem){
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
