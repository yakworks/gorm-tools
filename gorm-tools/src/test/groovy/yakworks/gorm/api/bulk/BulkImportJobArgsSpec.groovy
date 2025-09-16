package yakworks.gorm.api.bulk

import gorm.tools.job.DataLayout
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import yakworks.commons.beans.BeanTools
import yakworks.etl.DataMimeTypes

class BulkImportJobArgsSpec extends Specification  {

    void "test with map"() {
        when: "simple"
        Map paramMap = [
            source: 'foo',
            op: 'add',
            parallel: "False",
            attachmentId: '123'
        ]
        BulkImportJobArgs biParams = BulkImportJobArgs.fromParams(paramMap)

        then:
        //biParams.async //async by default
        biParams.parallel == false
        biParams.attachmentId == 123
        biParams.op == DataOp.add
        biParams.asMap() == [
            source: 'foo',
            op: DataOp.add,
            parallel: false,
            async: true,
            attachmentId: 123,
            dataLayout: DataLayout.Result,
            dataFormat: DataMimeTypes.json,
            jobType: 'bulk.import'
        ]

        when: "simple"
        paramMap = [
            op: 'upsert',
            parallel: "True",
            async: "no",
            includes: "a, b, c",
            foo: 1,
            bar: 'baz'
        ]
        biParams = BulkImportJobArgs.fromParams(paramMap)

        then:
        biParams.parallel == true
        biParams.async == false
        biParams.op == DataOp.upsert
        biParams.includes == ['a','b','c']
        biParams.queryParams.foo == 1
        biParams.queryParams.bar == 'baz'
    }

    void "test POC for BeanTools.bind - not working right"() {
        when:
        Map paramMap = [
            op: 'add',
            attachmentId: '123',
            parallel: "false",
            //includes: "a,b,c" // THIS DOES NOT WORK WITH Jackson Binder
        ]
        BulkImportJobArgs biParams = BeanTools.bind(paramMap, BulkImportJobArgs)

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
            includes: "a, b ,c", // THIS DOES NOT WORK WITH Jackson Binder
            foo: 1,
            bar: 'baz'
        ]
        BulkImportJobArgs biParams = BulkImportJobArgs.fromParams(paramMap)
        Map biMap = biParams.asMap()

        then:
        biMap.keySet().size() == 10 // [op, parallel, dataFormat, dataLayout, attachmentId, includes, jobType, foo, bar]
        biMap.parallel == false
        biMap.attachmentId == 123
        biMap.op == DataOp.add
        biMap.includes == ['a','b','c']
        biMap.foo == 1
        biMap.bar == 'baz'
        biMap.async == true

        when: "put map back into bulkImportJobArgs"
        BulkImportJobArgs biParams2 = BulkImportJobArgs.fromParams(paramMap)

        then:
        biParams2.parallel == false
        biParams2.attachmentId == 123
        biParams2.op == DataOp.add
        biParams2.includes == ['a','b','c']
        biParams2.queryParams.foo == 1
        biParams2.queryParams.bar == 'baz'
    }


}
