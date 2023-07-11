package gorm.tools.job

import spock.lang.Specification
import testing.TestSyncJobService

class SyncJobArgsSpec extends Specification  {

    void "test withParams"() {
        when: "defaults"
        SyncJobArgs args = SyncJobArgs.withParams([:])

        then:
        !args.async
        args.parallel == null
        args.savePayload
        !args.source
        !args.sourceId

        when: "explicitely provided"
        args = SyncJobArgs.withParams([parallel:true, async:true, savePayload: false, source:"test", sourceId:"test"])

        then:
        args.parallel
        args.async
        !args.savePayload
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

}
