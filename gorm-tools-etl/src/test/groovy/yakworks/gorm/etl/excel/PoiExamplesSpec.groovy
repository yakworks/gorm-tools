package yakworks.gorm.etl.excel

import builders.dsl.spreadsheet.builder.poi.PoiSpreadsheetBuilder
import spock.lang.Specification
import yakworks.commons.map.MapFlattener

class PoiExamplesSpec extends Specification {

    Map flattenMap(Map map){
        MapFlattener.of(map).convertObjectToString(false).flatten()
    }

    void "files exists"() {
        when:
        File file = new File('build/spreadsheet.xlsx')
        def dataList = [
            [id: 1, name: "Wal", balance: 100.00],
            [id: 2, name: "Belk", balance: 2000.00]
        ]
        def headers = dataList[0].keySet()

        PoiSpreadsheetBuilder.create(file).build {
            apply BookExcelStylesheet
            sheet('Sample') { s ->
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
                    // def flatData = flattenMap(rowData as Map)
                    def vals = headers.collect{ rowData[it]}

                    row {
                        vals.each { dta ->
                            cell {
                                value dta
                                if(dta instanceof BigDecimal){
                                    style {
                                        format "#,##0.00"
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        then:
        file.exists()
    }

}
