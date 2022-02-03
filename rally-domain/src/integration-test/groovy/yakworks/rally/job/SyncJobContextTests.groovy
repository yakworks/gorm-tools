package yakworks.rally.job

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.api.ResultUtils
import yakworks.commons.json.JsonEngine
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.Problem
import yakworks.problem.ProblemTrait

@Integration
@Rollback
class SyncJobContextTests extends Specification implements DomainIntTest {

    SyncJobService syncJobService

    SyncJobContext createJob(){
        def samplePaylod = [1,2,3,4]
        SyncJobArgs syncJobArgs = new SyncJobArgs(sourceId: '123', source: 'some source')
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, samplePaylod)
    }

    def "test create job"() {
        when:
        SyncJobContext jobContext = createJob()

        then:
        jobContext.jobId
        SyncJob.get(jobContext.jobId)

    }

    def "test update job"() {
        given:
        SyncJobContext jobContext = createJob()

        when:
        def apiRes = ApiResults.of("foo")
        jobContext.updateJob(apiRes, [id:jobContext.jobId , errorBytes: JsonEngine.toJson(apiRes).bytes ])

        then:
        SyncJob job = SyncJob.get(jobContext.jobId)
        job.errorBytes

    }

    def "test closure"() {
        given:
        SyncJobContext jobContext = createJob()
        String transformResultsClosureWasCalled

        jobContext.transformResultsClosure = {apiResults ->
            transformResultsClosureWasCalled = apiResults.title
        }

        when:
        def apiRes = ApiResults.of("foo").title("gogogo")
        jobContext.transformResults(apiRes)

        then:
        transformResultsClosureWasCalled == 'gogogo'

    }

    def "test finish job"() {
        given:
        SyncJobContext jobContext = createJob()
        List<Map> renderResults = [[ok: true, status: 200, data: ['boo':'foo']] ,
                                    [ok: true, status: 200, data: ['boo2':'foo2']] ]
        Problem  problem = Problem.ofCode('security.validation.password.error')
        List<Map> renderErrorResults = [[ok: false, status: 500, detail: 'error detail'] ]
        //do the failed
            when:
        //tests finish the job
        jobContext.finishJob(renderResults, renderErrorResults)

        then:
        SyncJob job = SyncJob.get(jobContext.jobId)
        job.errorBytes
        //XXX add errorBytes assert
        job.dataToString().contains("boo")
    }


}
