package yakworks.rest

import gorm.tools.transaction.WithTrx
import grails.testing.mixin.integration.Integration
import okhttp3.Response
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.http.HttpStatus
import spock.lang.Specification
import yakworks.rest.client.OkHttpRestTrait

import static yakworks.etl.excel.ExcelUtils.getHeader

@Integration
class ExcelExportSpec extends Specification implements OkHttpRestTrait, WithTrx {

    String path = "/api/rally/org"

    def setup(){
        login()
    }

    void "test xlsx"() {
        when:
        Response resp = get("${path}?q=*&format=xlsx")

        then:
        resp.code() == HttpStatus.OK.value()

        when: "verify excel file"
        XSSFWorkbook workbook = new XSSFWorkbook(resp.body().byteStream())
        List<String> headers = getHeader(workbook)

        then: "column name should have been resolved from grid col model"
        headers
        //flex.text1 does not have a label, so it would have used getNaturalTitle on name
        headers.containsAll(['Num', 'Name', 'Type', 'TotalDue', 'Flex Text1'])
    }
}
