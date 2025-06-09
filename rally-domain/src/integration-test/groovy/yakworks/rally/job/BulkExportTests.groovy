package yakworks.rally.job

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.model.DataOp
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException
import yakworks.gorm.api.bulk.BulkExportJobParams
import yakworks.gorm.api.bulk.BulkExportService
import yakworks.gorm.api.bulk.BulkImportJobParams
import yakworks.gorm.api.support.DataMimeTypes
import yakworks.meta.MetaMapList
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class BulkExportTests extends Specification implements DomainIntTest  {

    BulkExportService<Org> getBulkExportService(){
        BulkExportService.lookup(Org)
    }

    void "test setupSyncJobArgs"() {
        given:
        BulkExportJobParams jobParams = BulkExportJobParams.withParams([
            sourceId: "test-job", includes: ['id','name','info.phone'],
            q: '{"id":{"$gte":1}}'
        ])

        when:
        SyncJobArgs jobArgs = bulkExportService.setupSyncJobArgs(jobParams)

        then:
        noExceptionThrown()
        jobArgs
        jobArgs.jobType == BulkExportJobParams.JOB_TYPE
        jobArgs.sourceId == "test-job"
        jobArgs.queryArgs
        jobArgs.entityClass == Org
        jobArgs.includes == ['id','name','info.phone']
        jobArgs.dataLayout == SyncJobArgs.DataLayout.Payload
    }

    Long bulkExport(String q){
        // Map params = [
        //     parallel: false, async:false,
        //     source: "test", sourceId: "test-job", includes: "id,name,ext.name"
        // ]
        // params.q = q
        BulkExportJobParams bexParams = new BulkExportJobParams(
            sourceId: "test-job", includes: ['id','name','info.phone'],
            q: q
        )

        SyncJobEntity jobEnt = bulkExportService.queueJob(bexParams)
        //flushAndClear()
        SyncJobEntity jobEnt2 = bulkExportService.runJob(jobEnt.id)
        //flushAndClear()
        jobEnt.id
    }

    void "test queueExportJob"() {
        when:
        SyncJobEntity jobEnt = bulkExportService.queueJob(
            new BulkExportJobParams(
                q: '{"foo": "bar"}',
                includes: ["id", "name", "info.phone"],
                sourceId: "test-job"
            )
        )
        flushAndClear()
        assert jobEnt.id

        def job = SyncJob.get(jobEnt.id)
        Map params = job.params

        then:
        noExceptionThrown()
        job.jobType == BulkExportJobParams.JOB_TYPE
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'

        and: 'params have extra fields'
        params.q == '{"foo": "bar"}'
        params.entityClassName == 'yakworks.rally.orgs.model.Org'

    }

    void "test empty q param"() {
        when:
        Map params = [:]
        SyncJobEntity jobEnt = bulkExportService.queueJob(new BulkExportJobParams())

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.qRequired'
    }

    void "test eachPage with no data"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$lt":1}}'
        )
        def syncjobArgs =  new SyncJobArgs(
            sourceId: "test", includes: ["id", "name", "ext.name"],
            queryArgs: queryArgs
        )
        var ctx = SyncJobContext.of(syncjobArgs)

        List dataList = []
        bulkExportService.eachPage(ctx){ List dataPage ->
            dataList.addAll(dataPage)
        }

        then:
        dataList.size() == 0
    }


    void "success bulk export JSON"() {
        when:
        //KitchenSink.createKitchenSinks(300)
        Long jobId = bulkExport('{"id":{"$gte":1}}')
        flushAndClear()

        def job = SyncJob.get(jobId)

        then: "verify job"

        job != null
        //job.source == "test"
        job.sourceId == "test-job"
        //job.payloadBytes != null
        //job.dataBytes != null
        job.state == SyncJobState.Finished

        when: "verify job.data (job results)"
        def dataString = job.dataToString()
        List results = parseJson(dataString, List)

        then:
        dataString.startsWith('[\n{') //sanity check
        results != null
        results instanceof List
        results.size() == 50

        when:
        List sortedList = results.sort {
            it.id
        }
        //make sure paging is good and they are all different"
        Long lastId = 0
        sortedList.each {
            assert it.id > lastId
            lastId = it.id
        }

        then: "verify includes"
        sortedList[0].size() == 3 //id, project name, nested name
        //results[0].data.id == 1
        sortedList[0].name == "Org1"
        sortedList[0].info.phone == "1-800-1"

        cleanup:
        def attachment = Attachment.get(job.dataId)
        attachment.remove()

    }

    void "success bulk export CSV"() {
        when:
        BulkExportJobParams bexParams = new BulkExportJobParams(
            sourceId: "test-job", includes: ['id','name','info.phone'],
            q: '{"id":{"$gte":1}}', dataFormat: DataMimeTypes.csv
        )
        SyncJobEntity jobEnt = bulkExportService.queueJob(bexParams)
        SyncJob job = bulkExportService.runJob(jobEnt.id)

        flushAndClear()

        //def job = SyncJob.get(jobId)

        then: "verify job"

        job != null
        //job.source == "test"
        job.sourceId == "test-job"
        //job.payloadBytes != null
        //job.dataBytes != null
        job.state == SyncJobState.Finished

        when: "verify job.data (job results)"
        String dataString = job.dataToString()
        //List results = parseJson(dataString, List)
        List csvList = dataString.readLines()

        then:
        csvList.size() == 51 //50 rows plus header
        csvList[0] == '"id","name","info.phone"'
        csvList[1] == '"1","Org1","1-800-1"'
        csvList[50] == '"50","Org50","1-800-50"'

        cleanup:
        def attachment = Attachment.get(job.dataId)
        attachment.remove()
    }

    void "success bulk export large CSV"() {
        when:
        KitchenSink.createKitchenSinks(1000)
        BulkExportJobParams bexParams = new BulkExportJobParams(
            sourceId: "test-job", includes: ['id','name','ext.name'],
            q: '{"id":{"$gte":1}}', dataFormat: DataMimeTypes.csv
        )
        var bexService = BulkExportService.lookup(KitchenSink)
        SyncJobEntity jobEnt = bexService.queueJob(bexParams)
        SyncJob job = bexService.runJob(jobEnt.id)

        flushAndClear()

        //def job = SyncJob.get(jobId)

        then: "verify job"

        job != null
        //job.source == "test"
        job.sourceId == "test-job"
        //job.payloadBytes != null
        //job.dataBytes != null
        job.state == SyncJobState.Finished

        when: "verify job.data (job results)"
        String dataString = job.dataToString()
        //List results = parseJson(dataString, List)
        List csvList = dataString.readLines()

        then:
        csvList.size() == 1001 //1000 rows plus header
        csvList[0] == '"id","name","ext.name"'
        // csvList[1] == '"1","Org1","1-800-1"'
        csvList[1000] == '"1000","Squash","SinkExt1000"'

        cleanup:
        def attachment = Attachment.get(job.dataId)
        attachment.remove()
        KitchenSink.list().each{
            it.remove()
        }
    }

}
