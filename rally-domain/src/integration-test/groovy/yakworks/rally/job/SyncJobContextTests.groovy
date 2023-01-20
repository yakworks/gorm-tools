package yakworks.rally.job

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonException
import groovy.json.JsonSlurper
import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.json.groovy.JsonEngine
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Org

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class SyncJobContextTests extends Specification implements DomainIntTest {

    SyncJobService syncJobService

    SyncJobContext createJob(){
        def samplePaylod = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')
        syncJobArgs.entityClass = Org
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, samplePaylod)
    }

    void "sanity check JsonSlurper is not lax by default"() {
        when:
        new JsonSlurper().parseText('{"data": [{"one": 1}, {"two": 2},]}')

        then:
        JsonException ex = thrown()
    }

    void "test create job and save payload to file"() {
        when:
        List payload = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')
        syncJobArgs.entityClass = Org
        syncJobArgs.savePayload = true
        syncJobArgs.savePayloadAsFile = true
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, payload)

        then:
        noExceptionThrown()
        jobContext.jobId
        SyncJob.get(jobContext.jobId)
    }

    void "test create job"() {
        when:
        SyncJobContext jobContext = createJob()

        then:
        jobContext.jobId
        SyncJob.get(jobContext.jobId)
    }

    void "test update job with errors"() {
        given:
        SyncJobContext jobContext = createJob()

        when:
        def apiRes = ApiResults.ofPayload("foo")
        jobContext.updateJob(apiRes, [id:jobContext.jobId , errorBytes: JsonEngine.toJson(apiRes).bytes ])

        then:
        SyncJob job = SyncJob.get(jobContext.jobId)
        job.problemsBytes
    }

    void "test update job and save data to file generates valid json"() {
        given:
        SyncJobContext jobContext = createJob()
        jobContext.args.saveDataAsFile = true

        when:
        ApiResults apiRes = ApiResults.OK()
        (1..20).each {
            apiRes << Result.OK().payload([id:it, num:"num-$it", name: "name-$it"])
        }

        jobContext.updateJobResults(apiRes)
        jobContext.finishJob()

        then:
        noExceptionThrown()

        when:
        SyncJob job = SyncJob.get(jobContext.jobId)

        then:
        job != null
        job.dataId != null //should have a data id

        when: "Verify attachment"
        Attachment attachment = Attachment.get(job.dataId)

        then:
        attachment != null

        when:
        String jsonText = SyncJob.repo.dataToString(job)

        then:
        jsonText

        when:
        def json = new JsonSlurper().parseText(jsonText)

        then: "Should be valid json"
        noExceptionThrown()
        json != null
        json.size() == 20
        json[0].ok == true
        json[0].data.num  == "num-1"
        json[0].data.name  == "name-1"
        json[19].data.num  == "num-20"
        json[19].data.name  == "name-20"

        cleanup:
        if(attachment) attachment.remove()

    }

    def "test transform result closure"() {
        given:
        SyncJobContext jobContext = createJob()
        String transformResultsClosureWasCalled

        jobContext.transformResultsClosure = { apiResults ->
            transformResultsClosureWasCalled = apiResults.title
        }

        when:
        def apiRes = ApiResults.ofPayload("foo").title("gogogo")
        jobContext.transformResults(apiRes)

        then:
        transformResultsClosureWasCalled == 'gogogo'

    }

    def "test finish job dataFormat is Payload"() {
        given:
        List payload = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source', dataFormat: SyncJobArgs.DataFormat.Payload)
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, payload)

        def okResults = ApiResults.OK()
        okResults.add(Result.OK().payload(['boo':'foo']))
        okResults.add(Result.OK().payload(['boo2':'foo2']))

        when:
        jobContext.updateJobResults(okResults)
        jobContext.updateJobResults(Problem.ofPayload(['bad':'data']).title("Oops"))

        jobContext.finishJob()

        then:
        SyncJob job = SyncJob.get(jobContext.jobId)
        //job.errorBytes
        List jsonData = parseJson(job.dataToString())


        jsonData.size() == 2
        jsonData[0] == ['boo':'foo']

        List jsonErrors = parseJson(job.problemsToString())
        jsonErrors.size() == 1
        jsonErrors[0].ok == false
        jsonErrors[0].status == 400
        jsonErrors[0].title == "Oops"
    }

}
