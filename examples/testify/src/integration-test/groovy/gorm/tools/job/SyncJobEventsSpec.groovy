package gorm.tools.job

import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.testify.SyncjobEventListener

import javax.inject.Inject

@Integration
class SyncJobEventsSpec extends Specification {

    SyncJobService syncJobService
    @Inject SyncjobEventListener syncjobEventListener

    void "sanity check"() {
        expect:
        syncjobEventListener
    }

    void "test job events without an entity class"() {
        setup:
        def samplePaylod = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source', jobType: 'event.test.job')

        when: "without entityClass"
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, samplePaylod)

        then: "new item to payload should have been pushed by start listener"
        jobContext.args.payload.size() == 5
        jobContext.args.payload[4] == 5

        when:
        jobContext.finishJob()

        then: "one more item to listener should have been pushed"
        jobContext.args.payload.size() == 6
        jobContext.args.payload[5] == 6

    }
}
