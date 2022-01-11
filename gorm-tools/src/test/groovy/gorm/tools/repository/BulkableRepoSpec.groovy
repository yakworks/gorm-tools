package gorm.tools.repository

import gorm.tools.databinding.PathKeyMap
import gorm.tools.problem.ValidationProblem
import org.springframework.http.HttpStatus

import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobState
import gorm.tools.repository.bulk.BulkableArgs
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Specification
import testing.Cust
import testing.TestSyncJob
import testing.TestSyncJobService
import yakworks.gorm.testing.SecurityTest
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.KitchenSinkRepo
import yakworks.gorm.testing.model.SinkExt

import static yakworks.commons.json.JsonEngine.parseJson

class BulkableRepoSpec extends Specification implements DataRepoTest, SecurityTest {

    ParallelTools parallelTools
    AsyncService asyncService
    KitchenSinkRepo kitchenSinkRepo

    void setupSpec() {
        defineBeans{
            syncJobService(TestSyncJobService)
        }
        mockDomains(KitchenSink, SinkExt, TestSyncJob)
    }

    BulkableArgs setupBulkableArgs(DataOp op = DataOp.add){
        return new BulkableArgs(asyncEnabled: false, op: op,
            params:[source: "test", sourceId: "test"], includes: ["id", "name", "ext.name"])
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

    void "success bulk insert"() {
        given:
        List list = KitchenSink.generateDataList(20)

        when: "bulk insert 20 records"

        Long jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs())
        def job = TestSyncJob.get(jobId)


        then: "verify job"

        job != null
        job.source == "test"
        job.sourceId == "test"
        job.requestData != null
        job.data != null
        job.state == SyncJobState.Finished

        when: "Verify job.requestData (incoming json)"
        def payload = parseJson(job.requestDataToString())

        then:
        payload != null
        payload instanceof List
        payload.size() == 20
        payload[0].name == "Sink1"
        payload[0].ext.name == "SinkExt1"
        payload[19].name == "Sink20"

        when: "verify job.data (job results)"
        def dataString = job.dataToString()
        List results = parseJson(dataString, List)

        then:
        dataString.startsWith('[{') //sanity check
        results != null
        results instanceof List
        results.size() == 20
        results[0].ok == true
        results[0].status == HttpStatus.CREATED.value()
        results[10].ok == true

        and: "verify includes"
        results[0].data.size() == 3 //id, project name, nested name
        //results[0].data.id == 1
        results[0].data.name == "Sink1"
        results[0].data.ext.name == "SinkExt1"

        and: "Verify database records"
        KitchenSink.count() == 20
        KitchenSink.findByName("Sink1") != null
        KitchenSink.findByName("Sink1").ext.name == "SinkExt1"
        KitchenSink.findByName("Sink20") != null
    }

    void "test bulk update"() {
        List list = KitchenSink.generateDataList(10)

        when: "insert records"

        Long jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs())
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

        jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs(DataOp.update))
        job = TestSyncJob.get(jobId)

        then:
        noExceptionThrown()
        job != null
        job.data != null
        job.state == SyncJobState.Finished

        and: "Verify db records"
        KitchenSink.count() == 10
        //Project.get(1).name == "updated-1" XXX FIX, some how doesnt update in unit tests
        //Project.get(10).name == "updated-10"

    }

    @IgnoreRest
    void "test failures and errors"() {
        given:
        List list = KitchenSink.generateDataList(20)

        and: "Add a bad records"
        list[1].ext.name = null
        // list[19].ext.name = null

        when: "bulk insert"

        Long jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs())
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

        Long jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs())
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

    @Issue("domain9#413")
    void "test batching"() {
        setup: "Set batchSize of 10 to trigger batching/slicing"
        asyncService.sliceSize = 10
        List<Map> list = KitchenSink.generateDataList(60) //this should trigger 6 batches of 10

        when: "bulk insert in multi batches"
        Long jobId = kitchenSinkRepo.bulk(list, setupBulkableArgs())
        def job = TestSyncJob.findById(jobId)

        def results = parseJson(job.dataToString())

        then: "just 60 should have been inserted, not the entire list twice"
        results.size() == 60

        cleanup:
        asyncService.sliceSize = 50
    }

    void "success bulk insert with csv using usePathKeyMap"() {
        given:
        List data = [] as List<Map>

        data << PathKeyMap.of([num:'1', name:'Sink1', ext_name:'SinkExt1', bazMap_foo:'bar'], '_')
        data << PathKeyMap.of([num:'2', name:'Sink2', ext_name:'SinkExt2', bazMap_foo:'bar'], '_')

        when: "bulk insert 2 records"
        BulkableArgs args = setupBulkableArgs()
        Long jobId = kitchenSinkRepo.bulk(data, args)
        def job = TestSyncJob.get(jobId)


        then: "verify job"

        job != null
        job.source == "test"
        job.sourceId == "test"
        job.requestData != null
        job.data != null
        job.state == SyncJobState.Finished

        when: "Verify job.requestData (incoming json)"
        def payload = parseJson(job.requestDataToString())

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
        KitchenSink.findByName("Sink1") != null
        KitchenSink.findByName("Sink1").ext.name == "SinkExt1"
    }

}
