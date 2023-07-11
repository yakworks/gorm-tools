package gorm.tools.job

import spock.lang.Specification
import testing.TestSyncJobService

class SyncJobServiceSpec extends Specification  {

    void "test setupSyncJobArgs"() {
        setup:
        SyncJobService syncJobService = new TestSyncJobService()

        when: "defaults"
        SyncJobArgs args = syncJobService.setupSyncJobArgs([:])

        then:
        args.async
        !args.parallel
        args.savePayload
        !args.source
        !args.sourceId

        when: "explicitely provided"
        args = syncJobService.setupSyncJobArgs([parallel:true, async:true, savePayload: false, source:"test", sourceId:"test"])

        then:
        args.parallel
        args.async
        !args.savePayload
        args.source == "test"
        args.sourceId == "test"

        when:
        args = syncJobService.setupSyncJobArgs([async:false])

        then:
        !args.async
    }

}
