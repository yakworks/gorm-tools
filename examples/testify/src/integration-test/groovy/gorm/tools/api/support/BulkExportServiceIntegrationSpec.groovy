package gorm.tools.api.support

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobState
import yakworks.gorm.api.support.BulkExportService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest

import javax.inject.Inject

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class BulkExportServiceIntegrationSpec extends Specification implements DomainIntTest {

    @Inject BulkExportService bulkExportService

    void "test scheduleBulkExportJob"() {
        setup:
        SyncJobArgs args = setupJobArgs("type": "Company", "inactive":false)

        when:
        Long jobId = bulkExportService.scheduleBulkExportJob(args)

        then:
        noExceptionThrown()
        jobId

        when:
        SyncJob job = SyncJob.get(jobId)

        then:
        job
        job.state == SyncJobState.Queued

        when:
        String payload = job.payloadToString()

        then:
        payload

        when:
        def payloadJson = parseJson(payload)

        then:
        payloadJson
        payloadJson.q == ["type": "Company", "inactive":false]
        payloadJson.includes == ["id", "num", "name"]
    }

    void "test buildJobContext and args "() {
        setup:
        Long jobId = bulkExportService.scheduleBulkExportJob(setupJobArgs("type": "Company", "inactive":false))

        when:
        SyncJobContext context = bulkExportService.buildJobContext(jobId)
        SyncJobArgs args = context.args

        then:
        noExceptionThrown()
        context
        context.syncJobService
        args
        args.queryArgs
        args.queryArgs.criteriaMap == ["type": "Company", "inactive":false]
        args.includes == ["id", "num", "name"]
        args.saveDataAsFile
        args.async
        args.parallel
    }

    void "run export"() {
        setup:
        Long jobId = bulkExportService.scheduleBulkExportJob(setupJobArgs("inactive": false))

        when:
        bulkExportService.runBulkExportJob(jobId, false)
        flushAndClear()

        then:
        noExceptionThrown()

        when:
        SyncJob job = SyncJob.repo.getWithTrx(jobId)


        then:
        job
        job.ok
        job.dataId //should have attachment
        Attachment.repo.exists(job.dataId)

        when:
        String jsonStr = job.dataToString()
        def json = parseJson(jsonStr)

        then:
        json
        json instanceof List
        json.size() == Org.findAllWhere(inactive:false).size() / 10
        json[0].data instanceof List

        when:
        List data = json[0].data

        then:
        //syncjob data format
        /*
         * {
         *   data: [
         *      {id:1, name:"x", num:"y"},
         *      {id:2, name:"x", num:"y"},
         *   ]
         * }
         */
        data.size() == 10 //
        data[0].id == 1
        data[0].num
        data[0].name


        cleanup:
        if(job && job.dataId) {
            Attachment.repo.removeById(job.dataId)
        }
    }

    SyncJobArgs setupJobArgs(Map q) {
        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(q:q)
        syncJobArgs.includes = ["id", "num", "name"]
        syncJobArgs.jobState = SyncJobState.Queued
        syncJobArgs.entityClass = Org
        return syncJobArgs
    }

}
