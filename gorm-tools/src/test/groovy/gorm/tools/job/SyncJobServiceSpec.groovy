package gorm.tools.job

import grails.testing.spring.AutowiredTest
import spock.lang.Specification
import testing.TestSyncJobService
import yakworks.testing.gorm.unit.DataRepoTest

class SyncJobServiceSpec extends Specification implements DataRepoTest, AutowiredTest {
    static springBeans = [
        syncJobService: TestSyncJobService
    ]

    SyncJobService syncJobService

    void "test setupSyncJobArgs"() {
        when: "defaults"
        SyncJobArgs args = syncJobService.setupSyncJobArgs([:])

        then:
        args.async
        !args.parallel
        args.savePayload
        !args.source
        !args.sourceId

        when: "explicitely provided"
        args = syncJobService.setupSyncJobArgs([paralle:true, async:true, savePayload: false, source:"test", sourceId:"test"])

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
