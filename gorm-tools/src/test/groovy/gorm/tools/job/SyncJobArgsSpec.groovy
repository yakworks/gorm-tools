package gorm.tools.job

import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import testing.TestSyncJobService
import yakworks.commons.lang.EnumUtils

class SyncJobArgsSpec extends Specification  {

    void "test withParams"() {
        when: "defaults"
        SyncJobArgs args = SyncJobArgs.withParams([:])

        then:
        args.async //async by default
        args.parallel == null
        !args.source
        !args.sourceId

        when: "explicitely provided"
        args = SyncJobArgs.withParams([parallel:true, async:false, source:"test", sourceId:"test"])

        then:
        args.parallel
        !args.async
        args.source == "test"
        args.sourceId == "test"

        when: "make sure the others work"
        args = SyncJobArgs.withParams([async:false, jobSource: "foo", dataFormat: "payload"])

        then:
        !args.async
        args.source == "foo"
        !args.sourceId
        args.dataFormat == SyncJobArgs.DataFormat.Payload
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

}
