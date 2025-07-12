package gorm.tools.job

import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import yakworks.commons.lang.EnumUtils
import yakworks.etl.DataMimeTypes

class SyncJobArgsSpec extends Specification  {

    void "test MapConstructor"() {
        when: "defaults"
        SyncJobArgs args = new SyncJobArgs(queryParams: [foo: 'bar'])

        then:
        args.queryParams == [foo: 'bar']

    }

    void "test withParams"() {
        when: "defaults"
        SyncJobArgs args = SyncJobArgs.withParams([:])

        then:
        args.async //async by default
        args.parallel == null
        !args.source
        !args.sourceId

        when: "explicitely provided"
        args = SyncJobArgs.withParams([parallel:true, async:false, source:"test", sourceId:"test", dataFormat: 'csv'])

        then:
        args.parallel
        !args.async
        args.source == "test"
        args.sourceId == "test"
        args.dataFormat == DataMimeTypes.csv

        when: "make sure the others work"
        args = SyncJobArgs.withParams([async:false, jobSource: "foo", dataLayout: "payload"])

        then:
        !args.async
        args.source == "foo"
        !args.sourceId
        args.dataLayout == SyncJobArgs.DataLayout.Payload
    }

    void "test enum"() {
        setup:
        //def dfoo = ("foo" as DataOp)
        def v = null
        def dfoo = EnumUtils.getEnumIgnoreCase(DataOp, v as String)
        expect:
        dfoo == null
    }

    void "test groovy as for enum casting"() {
        when:
        String addVal = 'add'
        DataOp addOp = addVal as DataOp

        String nullVal
        DataOp nullOp = EnumUtils.getEnumIgnoreCase(DataOp, nullVal)

        then:
        !nullOp
        addOp == DataOp.add

        when: "bad val"
        String fooVal = 'foo'
        DataOp fooOp = fooVal as DataOp

        then:
        thrown(IllegalArgumentException)

        when: "EnumUtils does not throw"
        fooOp = EnumUtils.getEnumIgnoreCase(DataOp, nullVal)

        then:
        !fooOp

    }

    void "test builder"() {
        when: "defaults"
        SyncJobArgs args = SyncJobArgs.withParams([foo: 'bar'])
            .includes(['id'])
            .sourceId("test")

        then:
        args.queryParams == [foo: 'bar']
        args.includes == ['id']
        args.sourceId == "test"

    }

}
