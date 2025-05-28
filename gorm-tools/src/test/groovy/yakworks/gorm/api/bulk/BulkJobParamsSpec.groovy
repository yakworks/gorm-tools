package yakworks.gorm.api.bulk

import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import yakworks.commons.beans.BeanTools

class BulkJobParamsSpec extends Specification  {

    void "test with map"() {
        when: "simple"
        Map paramMap = [
            op: 'add',
            attachmentId: '123',
            parallel: "False"
        ]
        BulkImportJobParams biParams = BulkImportJobParams.withParams(paramMap)

        then:
        //biParams.async //async by default
        biParams.parallel == false
        biParams.attachmentId == 123
        biParams.op == DataOp.add

        when: "simple"
        paramMap = [
            op: 'upsert',
            parallel: "True",
            async: "no",
            includes: "a, b, c"
        ]
        biParams = BulkImportJobParams.withParams(paramMap)

        then:
        biParams.parallel == true
        biParams.async == false
        biParams.op == DataOp.upsert
        biParams.includes == ['a','b','c']
    }

    void "test POC for BeanTools.bind - not working right"() {
        when:
        Map paramMap = [
            op: 'add',
            attachmentId: '123',
            parallel: "false",
            //includes: "a,b,c" // THIS DOES NOT WORK WITH Jackson Binder
        ]
        BulkImportJobParams biParams = BeanTools.bind(paramMap, BulkImportJobParams)

        then:
        biParams.parallel == false
        biParams.attachmentId == 123
        biParams.op == DataOp.add
        //biParams.includes == ['a','b','c']

    }

    void "test asMap"() {
        when:
        Map paramMap = [
            op: 'add',
            attachmentId: '123',
            parallel: "false",
            includes: "a,b,c" // THIS DOES NOT WORK WITH Jackson Binder
        ]
        BulkImportJobParams biParams = BulkImportJobParams.withParams(paramMap)
        Map biMap = biParams.asMap()

        then:
        biMap.keySet().size() == 4
        biMap.parallel == false
        biMap.attachmentId == 123
        biMap.op == DataOp.add
        biMap.includes == ['a','b','c']

    }


}
