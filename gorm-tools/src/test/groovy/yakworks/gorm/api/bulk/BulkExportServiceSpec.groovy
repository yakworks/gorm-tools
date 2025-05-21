package yakworks.gorm.api.bulk

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.model.DataOp
import gorm.tools.utils.BenchmarkHelper
import spock.lang.Specification
import testing.TestSyncJob
import testing.TestSyncJobService
import yakworks.api.problem.data.DataProblemException
import yakworks.gorm.config.AsyncConfig
import yakworks.gorm.config.GormConfig
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

import static yakworks.json.groovy.JsonEngine.parseJson

class BulkExportServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, SinkItem, TestSyncJob]
    static springBeans = [TestSyncJobService]

    // @Transactional
    void setupSpec() {
        KitchenSink.createKitchenSinks(300)
    }

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(
            parallel: false, async:false, op: op, jobType: 'bulk.import',
            source: "test", sourceId: "test", includes: ["id", "name", "ext.name"]
        )
    }

    BulkExportService<KitchenSink> getBulkExportService(){
        BulkExportService.lookup(KitchenSink)
    }

    Long bulkExport(String q){
        Map params = [
            parallel: false, async:false,
            source: "test", sourceId: "test-job", includes: "id,name,ext.name"
        ]
        params.q = q

        SyncJobEntity jobEnt = bulkExportService.queueExportJob(params, "test-job")
        //flushAndClear()
        SyncJobEntity jobEnt2 = bulkExportService.startJob(jobEnt.id)
        //flushAndClear()
        jobEnt.id
    }

    void "test queueExportJob"() {
        when:
        SyncJobEntity jobEnt = bulkExportService.queueExportJob(
            [
                q: [foo: 'bar'],
                includes: ["id", "name", "ext.name"]
            ], //params
            "test-job" //sourceId
        )
        flushAndClear()
        assert jobEnt.id

        def job = TestSyncJob.get(jobEnt.id)
        Map params = job.params

        then:
        noExceptionThrown()
        job.jobType == 'bulk.export'
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'

        and: 'params have extra fields'
        params.q == [foo: 'bar']
        params.entityClassName == 'yakworks.testing.gorm.model.KitchenSink'

    }

    void "test empty q param"() {
        when:
        Map params = [:]
        SyncJobEntity jobEnt = bulkExportService.queueExportJob(params, "test-job")

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.qRequired'
    }

    void "test eachPage"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$gte":1}}'
        )

        List dataList = []
        bulkExportService.eachPage(queryArgs){ List dataPage ->
            dataList.addAll(dataPage)
        }

        List sortedList = dataList.sort {
            it.id
        }
        //make sure paging is good and they are all different"
        Long lastId = 0
        sortedList.each {
            assert it.id > lastId
            lastId = it.id
        }

        then:
        sortedList.size() == 300
    }

    void "test eachPage with no data"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$lt":1}}'
        )

        List dataList = []
        bulkExportService.eachPage(queryArgs){ List dataPage ->
            dataList.addAll(dataPage)
        }

        then:
        dataList.size() == 0
    }


    void "success bulk export"() {

        when:
        Long jobId = bulkExport('{"id":{"$gte":1}}')
        flushAndClear()

        def job = TestSyncJob.get(jobId)

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
        dataString.startsWith('[{') //sanity check
        results != null
        results instanceof List
        results.size() == 300

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
        sortedList[0].name == "Blue Cheese"
        sortedList[0].ext.name == "SinkExt1"


    }

}
