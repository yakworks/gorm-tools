package gorm.tools.repository

import gorm.tools.async.AsyncService
import yakworks.gorm.config.AsyncConfig
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobState
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.model.DataOp
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import spock.lang.Specification
import testing.TestSyncJob
import testing.TestSyncJobService
import yakworks.commons.map.PathKeyMap
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.unit.GormHibernateTest

import static yakworks.json.groovy.JsonEngine.parseJson

class BulkableRepoSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, TestSyncJob]
    static springBeans = [syncJobService: TestSyncJobService]

    @Autowired AsyncConfig asyncConfig
    @Autowired AsyncService asyncService
    @Autowired KitchenSinkRepo kitchenSinkRepo

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(asyncEnabled: false, op: op, source: "test", sourceId: "test",
            includes: ["id", "name", "ext.name"])
    }

    void "sanity check bulkable repo"() {
        expect:
        KitchenSink.repo instanceof BulkableRepo
    }


    def "sanity check single validation"() {
        when:
        def ksdata = KitchenSink.repo.generateData(1)
        ksdata.ext.name = ''
        KitchenSink.create(ksdata)

        then:
        thrown(ValidationProblem.Exception)
    }

    void "simple bulk insert"() {
        given:
        List list = KitchenSink.generateDataList(10)

        when: "bulk insert 20 records"
        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
        def job = TestSyncJob.get(jobId)
        List results = job.parseData()

        then: "verify job"
        job.state == SyncJobState.Finished
        results[0].ok
    }

    void "success bulk insert"() {
        given:
        List list = KitchenSink.generateDataList(300)

        when: "bulk insert 20 records"

        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
        def job = TestSyncJob.get(jobId)


        then: "verify job"

        job != null
        job.source == "test"
        job.sourceId == "test"
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
        List results = parseJson(dataString, List)

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
        def bcks = KitchenSink.findByName("Blue Cheese")
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
        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
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

        jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs(DataOp.update))
        job = TestSyncJob.get(jobId)

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

        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
        def job = TestSyncJob.get(jobId)

        def results = parseJson(job.dataToString())

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

        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
        def job = TestSyncJob.get(jobId)

        then: "verify job"
        job.ok == false

        when: "verify job.data"
        def results = parseJson(job.dataToString())

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
        setup: "Set batchSize of 10 to trigger batching/slicing"
        asyncConfig.sliceSize = 10
        List<Map> list = KitchenSink.generateDataList(60) //this should trigger 6 batches of 10

        when: "bulk insert in multi batches"
        Long jobId = kitchenSinkRepo.bulk(list, setupSyncJobArgs())
        def job = TestSyncJob.findById(jobId)

        def results = parseJson(job.dataToString())

        then: "just 60 should have been inserted, not the entire list twice"
        results.size() == 60

        cleanup:
        asyncConfig.sliceSize = 50
    }

    void "success bulk insert with csv using usePathKeyMap"() {
        given:
        List data = [] as List<Map>

        data << PathKeyMap.of([num:'1', name:'Sink1', ext_name:'SinkExt1', bazMap_foo:'bar'], '_')
        data << PathKeyMap.of([num:'2', name:'Sink2', ext_name:'SinkExt2', bazMap_foo:'bar'], '_')

        when: "bulk insert 2 records"
        SyncJobArgs args = setupSyncJobArgs()
        Long jobId = kitchenSinkRepo.bulk(data, args)
        def job = TestSyncJob.get(jobId)


        then: "verify job"

        job != null
        job.source == "test"
        job.sourceId == "test"
        job.payloadBytes != null
        job.dataBytes != null
        job.state == SyncJobState.Finished

        when: "Verify job.payload (incoming json)"
        def payload = parseJson(job.payloadToString())

        then:
        payload != null
        payload instanceof List
        payload.size() == 2
//        payload[0].name == "Sink1"
//        payload[0].ext.name == "SinkExt1"
//        payload[1].name == "Sink2"

        when: "verify job.data (job results)"
        def dataString = job.dataToString()
        List results = parseJson(dataString, List)

        then:
        dataString.startsWith('[{') //sanity check
        results != null
        results instanceof List
        results.size() == 2
        results[0].ok == true
        results[0].status == HttpStatus.CREATED.value()
        results[1].ok == true

        and: "verify includes"
        results[0].data.size() == 3 //id, project name, nested name
        //results[0].data.id == 1
        results[0].data.name == "Sink1"
        results[0].data.ext.name == "SinkExt1"

        and: "Verify database records"
        KitchenSink.count() == 2
        KitchenSink.withSession {
            assert KitchenSink.findByName("Sink1") != null
            assert KitchenSink.findByName("Sink1").ext.name == "SinkExt1"
            true
        }
    }

}
