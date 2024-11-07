/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.etl.excel

import groovy.transform.CompileStatic

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

@CompileStatic
class ExcelUtils {

    static List<String> getHeader(XSSFWorkbook workbook) {
        XSSFSheet firstSheet = workbook.getSheetAt(0)
        XSSFRow row = firstSheet.getRow(0)
        List<String> header = []

        for (Cell cell : row) {
            header.add(cell.getStringCellValue())
        }

        return header
    }
}
