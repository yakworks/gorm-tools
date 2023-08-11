/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.etl.excel

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

import org.apache.poi.ss.usermodel.CellStyle

import builders.dsl.spreadsheet.builder.api.SpreadsheetBuilder
import builders.dsl.spreadsheet.builder.poi.PoiSpreadsheetBuilder
import yakworks.commons.lang.DateUtil
import yakworks.commons.lang.PropertyTools
import yakworks.commons.map.MapFlattener

/**
 * POC to use th4e groovy SpreadSheet builded that wraps poi.
 * Works well but is almost twice as slow, might be because the styles
 */
@SuppressWarnings(['NestedBlockDepth'])
@CompileStatic
class XlsxSpreadsheetBuilder {

    Set<String> headers
    OutputStream outputStream
    public static final String SHEET_NAME = "data"

    CellStyle moneyStyle
    CellStyle localDateStyle
    CellStyle dateTimeStyle

    XlsxSpreadsheetBuilder(OutputStream outstream){
        this.outputStream = outstream
    }

    static XlsxSpreadsheetBuilder of(OutputStream outstream){
        return new XlsxSpreadsheetBuilder(outstream)
    }

    void writeXlsx(List<Map> dataList){
        //flatten
        // BenchmarkHelper.startTime()
        Map<String, Object> firstRow = dataList[0] as Map<String, Object>
        Map flatRow = flattenMap(firstRow)
        headers = flatRow.keySet() as Set<String>

        SpreadsheetBuilder sb = PoiSpreadsheetBuilder.create(outputStream)
        // def psb = new PoiSpreadsheetBuilder(new XSSFWorkbook(), out)
        // assert sb.workbook
        sb.build {
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
                    // get all the values for the masterHeaders keys
                    // def flatData = flattenMap(rowData as Map)
                    def vals = headers.collect{ PropertyTools.getProperty(rowData, it) }
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
                                        format "mm/dd/yyyy"
                                    }
                                } else {
                                    value dta
                                }
                            }
                        }
                    }
                }
            }
        }
        // BenchmarkHelper.printEndTimeMsg("excel generate took")
    }

    Map flattenMap(Map map){
        MapFlattener.of(map).convertObjectToString(false).flatten()
    }

}
