package gorm.tools.repository

import org.springframework.http.HttpStatus

import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobState
import gorm.tools.repository.bulk.BulkableArgs
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Issue
import spock.lang.Specification
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
        results[0].data.id == 1
        results[0].data.name == "Sink1"
        results[0].data.ext.name == "SinkExt1"

        and: "Verify database records"
        KitchenSink.count() == 20
        KitchenSink.get(1).name == "Sink1"
        KitchenSink.get(1).ext.name == "SinkExt1"
        KitchenSink.get(20).name == "Sink20"
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

    void "test failures and errors"() {
        given:
        List list = KitchenSink.generateDataList(20)

        and: "Add few bad records"
        list[9].name = null
        list[19].ext.name = ""

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
        results.size() == 20

        and: "verify successfull results"
        results.findAll({ it.ok == true}).size() == 18
        results[0].ok == true

        and: "Verify failed records"
        results[9].ok == false
        results[9].data != null
        results[9].data.name == null
        results[9].data.ext.name == "SinkExt10"
        results[9].status == HttpStatus.UNPROCESSABLE_ENTITY.value()

        //results[9].title != null
        results[9].errors.size() == 1
        results[9].errors[0].field == "name"
        //results[9].errors[0].message == ""

        results[19].errors.size() == 1
        results[19].errors[0].field == "ext.name"
        //results[19].errors[0].field == "name"
    }


    // @IgnoreRest
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

}
