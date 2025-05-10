package gorm.tools.job

import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification

@Ignore //XXX @SUD fix
@Integration
class SyncJobEventsSpec extends Specification {
    SyncJobService syncJobService

    void "test job events without an entity class"() {
        setup:
        def samplePaylod = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')

        when: "without entityClass"
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, samplePaylod)

        then: "new item to payload should have been pushed by start listener"
        jobContext.payload.size() == 5
        jobContext.payload[4] == 5

        when:
        jobContext.finishJob()

        then: "one more item to listener should have been pushed"
        jobContext.payload.size() == 6
        jobContext.payload[5] == 6

    }
}
