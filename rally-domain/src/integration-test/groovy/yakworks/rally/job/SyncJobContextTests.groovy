package yakworks.rally.job

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.ApiResults
import yakworks.gorm.testing.DomainIntTest
import yakworks.problem.data.DataProblem
import yakworks.problem.data.DataProblemCodes
import yakworks.problem.data.DataProblemException
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactFlex
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.ContactSource
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.ContactRepo

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
        jobContext.updateJob(apiRes, [id:jobContext.jobId ....])

        then:
        //did it work?

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

        when:
        //tests finish the job
        jobContext.finishJob(...)

        then:
        //did it work?

    }


}
