package yakworks.gorm.api.bulk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import testing.TestSyncJob
import testing.TestSyncJobService
import yakworks.api.problem.data.DataProblemException
import yakworks.etl.DataMimeTypes
import yakworks.gorm.config.AsyncConfig
import yakworks.gorm.config.GormConfig
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.unit.GormHibernateTest

import static yakworks.json.groovy.JsonEngine.parseJson

class BulkImportServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, TestSyncJob]
    static springBeans = [TestSyncJobService]

    @Autowired AsyncConfig asyncConfig
    @Autowired GormConfig gormConfig

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(
            parallel: false, async:false, op: op, jobType: BulkImportJobArgs.JOB_TYPE,
            source: "test", sourceId: "test", includes: ["id", "name", "ext.name"]
        )
    }

    BulkImportService<KitchenSink> getBulkImportService(){
        BulkImportService.lookup(KitchenSink)
    }

    Long bulkImport(List dataList, DataOp op = DataOp.add){
        def bimpParams = new BulkImportJobArgs( op: op,
            parallel: false, async:false,
            source: "test", sourceId: "test-job", includes: ["id", "name", "ext.name"]
        )
        SyncJobEntity jobEnt = bulkImportService.queueAndRun(bimpParams, dataList)
        jobEnt.id
    }

    void "test queueImportJob with attachmentId"() {
        // setup:
        // gormConfig.legacyBulk = true

        when:
        def bimpParams = new BulkImportJobArgs(
            op: DataOp.add,
            sourceId: 'test-job',
            q: "{foo: 'bar'}", attachmentId: 1L
        )
        SyncJobEntity jobEnt = bulkImportService.queueJob(bimpParams, [])
        flushAndClear()
        assert jobEnt.id

        def job = TestSyncJob.get(jobEnt.id)
        Map params = job.params

        then:
        noExceptionThrown()
        job.jobType == BulkImportJobArgs.JOB_TYPE
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'
        job.payloadId == 1L

        and: 'params have extra fields'
        params.attachmentId == 1
        params.q == "{foo: 'bar'}" //[foo: 'bar']
        params.op == 'add'
        params.entityClassName == 'yakworks.testing.gorm.model.KitchenSink'


    }

    void "test queueImportJob with data"() {
        setup:
        gormConfig.legacyBulk = true
        List list = KitchenSink.generateDataList(10)

        when:
        def bimpParams = new BulkImportJobArgs(
            op: DataOp.add,
            sourceId: 'test-job'
        )
        SyncJobEntity jobEnt = bulkImportService.queueJob(bimpParams, list)

        flushAndClear()
        assert jobEnt.id

        def job = TestSyncJob.get(jobEnt.id)
        List payload = job.parsePayload()
        Map params = job.params

        then:
        noExceptionThrown()
        !job.payloadId
        payload.size() == list.size()

        and: 'params have extra fields'
        params.op == 'add'
        params.entityClassName == 'yakworks.testing.gorm.model.KitchenSink'
    }

    void "success bulk insert"() {
        given:
        List list = KitchenSink.generateDataList(300)

        when: "bulk insert 20 records"
        Long jobId = bulkImport(list)

        def job = TestSyncJob.get(jobId)

        then: "verify job"

        job != null
        job.source == "test"
        job.sourceId == "test-job"
        job.payloadBytes != null
        job.dataBytes != null
        job.state == SyncJobState.Finished

        when: "Verify payload"
        def payload = parseJson(job.payloadToString())

        then:
        payload != null
        payload instanceof List
        payload.size() == 300
        payload[0].name == "Blue Cheese"
        payload[0].ext.name == "SinkExt1"
        //sanity check
        payload[9].name == "Oranges"

        when: "verify job.data (job results)"
        def dataString = job.dataToString()
        List results = job.dataList

        then:
        dataString.startsWith('[{') //sanity check
        results != null
        results instanceof List
        results.size() == 300
        results[0].ok == true
        results[0].status == HttpStatus.CREATED.value()
        results[10].ok == true

        and: "verify includes"
        results[0].data.size() == 3 //id, project name, nested name
        //results[0].data.id == 1
        results[0].data.name == "Blue Cheese"
        results[0].data.ext.name == "SinkExt1"

        and: "Verify database records"
        def bcks = KitchenSink.findWhere(name: "Blue Cheese")
        bcks
        bcks.ext.name == "SinkExt1"

        KitchenSink.count() == 300

        and: "make sure beforeBulk event updated job id"
        bcks.createdByJobId
        // KitchenSink.findByName("Oranges")
    }

    void "test bulk update"() {
        List list = KitchenSink.generateDataList(10)

        when: "insert records"
        Long jobId = bulkImport(list)
        def job = TestSyncJob.get(jobId)

        then:
        job.state == SyncJobState.Finished

        and: "Verify db records"
        KitchenSink.count() == 10

        when: "Bulk update"
        list.eachWithIndex {it, idx ->
            it.name = "updated-${idx + 1}"
            it.id = idx + 1
        }

        Long jobIdUp = bulkImport(list)

        job = TestSyncJob.get(jobIdUp)

        then:
        noExceptionThrown()
        job != null
        job.dataToString() != '[]'
        job.state == SyncJobState.Finished

        and: "Verify db records"
        KitchenSink.count() == 10

    }

    void "test failures and errors"() {
        given:
        List list = KitchenSink.generateDataList(20)

        and: "Add a bad records"
        list[1].ext.name = null
        // list[19].ext.name = null

        when: "bulk insert"

        Long jobId = bulkImport(list)
        def job = TestSyncJob.get(jobId)

        def results = job.dataList

        then:
        job.ok == false
        results != null
        results instanceof List
        results.size() == 20

        and: "verify successfull results"
        results.findAll({ it.ok == true}).size() == 19
        results[0].ok == true

        and: "Verify failed record"
        results[1].ok == false
        results[1].data != null
        results[1].data.ext.name == null
        results[1].status == HttpStatus.UNPROCESSABLE_ENTITY.value()

        //results[9].title != null
        results[1].errors.size() == 1
        results[1].errors[0].field == "ext.name"
        //results[9].errors[0].message == ""

        // results[19].errors.size() == 1
        // results[19].errors[0].field == "ext.name"
        //results[19].errors[0].field == "name"
    }

    void "test failures and errors with customer and source"() {
        given:
        List list = KitchenSink.generateDataList(3)

        and: "Add few bad records"
        list[1].source = ['sourceId': '123']
        list[1].customer = ['sourceId': 'cust123']
        list[1].name = null


        when: "bulk insert"

        Long jobId = bulkImport(list)
        def job = TestSyncJob.get(jobId)

        then: "verify job"
        job.ok == false

        when: "verify job.data"
        def results = job.dataList

        then:
        results != null
        results instanceof List
        results.size() == 3

        and: "verify successfull results"
        results.findAll({ it.ok == true}).size() == 2
        results[0].ok == true

        and: "Verify failed records"
        results[1].ok == false
        results[1].data != null
        results[1].data.name == null
        results[1].data.source.sourceId == "123"
        results[1].data.customer.sourceId == "cust123"
        results[1].status == HttpStatus.UNPROCESSABLE_ENTITY.value()
    }

    void "test batching"() {
        when: "bulk insert in multi batches"
        asyncConfig.sliceSize = 10
        List<Map> list = KitchenSink.generateDataList(60) //this should trigger 6 batches of 10
        Long jobId = bulkImport(list)

        def job = TestSyncJob.get(jobId)

        def results = job.dataList

        then: "just 60 should have been inserted, not the entire list twice"
        results.size() == 60

        cleanup:
        asyncConfig.sliceSize = 50
    }

    void "test empty payload"() {
        when:
        def bimpParams = new BulkImportJobArgs(op: DataOp.add, sourceId: 'test-job')
        SyncJobEntity jobEnt = bulkImportService.queueJob(bimpParams, [])

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.data.emptyPayload'
    }

    void "test getPayloadData"() {
        setup:
        List list = KitchenSink.generateDataList(10)

        def bimpParams = new BulkImportJobArgs(
            op: DataOp.add,
            sourceId: 'test-job'
        )

        when:
        SyncJobEntity jobEnt = bulkImportService.queueJob(bimpParams, list)
        flushAndClear()

        then:
        jobEnt.id

        when:
        SyncJobEntity job =  TestSyncJob.get(jobEnt.id)
        BulkImportJobArgs jobParams = BulkImportJobArgs.fromParams(job.params)

        List<Map> data = bulkImportService.getPayloadData(jobEnt, jobParams)

        then:
        data
        data.size() == 10
    }

    void "test getPayloadData with attachment payload"() {
        setup:
        BulkImportService service = bulkImportService
        List list = KitchenSink.generateDataList(10)
        def bimpParams = new BulkImportJobArgs(
            op: DataOp.add,
            sourceId: 'test-job',
            payloadFormat: DataMimeTypes.csv,
            attachmentId: 100L
        )

        CsvToMapTransformer csvToMapTransformer = Mock()

        when:
        service.csvToMapTransformer = csvToMapTransformer
        SyncJobEntity jobEnt = service.queueJob(bimpParams, list)
        flushAndClear()

        then:
        jobEnt.id

        when:
        SyncJobEntity job =  TestSyncJob.get(jobEnt.id)
        BulkImportJobArgs jobParams = BulkImportJobArgs.fromParams(job.params)

        List<Map> data = service.getPayloadData(jobEnt, jobParams)

        then:
        1 * csvToMapTransformer.process(_) >> [[test:"data"]]
        data
        data.size() == 1

        // when: "payloadFormat json with attachment is not supported"
        // jobParams.payloadFormat = DataMimeTypes.json
        // service.getPayloadData(jobEnt, jobParams)
        //
        // then:
        // DataProblemException ex = thrown()
        // ex.detail == 'JSON attachment not yet supported'
    }
}
