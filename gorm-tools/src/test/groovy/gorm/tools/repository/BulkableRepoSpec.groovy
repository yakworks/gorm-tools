package gorm.tools.repository

import gorm.tools.async.AsyncService
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobState
import gorm.tools.json.JsonParserTrait
import gorm.tools.repository.bulk.BulkableArgs
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.testing.unit.DataRepoTest
import org.springframework.http.HttpStatus
import spock.lang.Issue
import spock.lang.Specification
import testing.TestRepoSyncJob
import testing.Nested
import testing.Project
import testing.ProjectRepo
import testing.TestRepoSyncJobService

class BulkableRepoSpec extends Specification implements DataRepoTest, JsonParserTrait {

    ParallelTools parallelTools
    AsyncService asyncService
    ProjectRepo projectRepo

    void setupSpec() {
        defineBeans{
            repoJobService(TestRepoSyncJobService)
        }
        mockDomains(Project, Nested, TestRepoSyncJob)
    }

    BulkableArgs setupBulkableArgs(DataOp op = DataOp.add){
        return new BulkableArgs(asyncEnabled: false, op: op,
            params:[source: "test", sourceId: "test"], includes: ["id", "name", "nested.name"])
    }

    void "sanity check bulkable repo"() {
        expect:
        Project.repo instanceof BulkableRepo
    }

    void "test bulk insert"() {
        given:
        List list = generateDataList(20)

        when: "bulk insert 20 records"
        Long jobId = projectRepo.bulk(list, setupBulkableArgs())
        def job = TestRepoSyncJob.get(jobId)

        then: "verify job"
        job != null
        job.source == "test"
        job.sourceId == "test"
        job.requestData != null
        job.data != null
        job.state == SyncJobState.Finished

        when: "Verify job.requestData (incoming json)"
        def payload = parseJsonBytes(job.requestData)

        then:
        payload != null
        payload instanceof List
        payload.size() == 20
        payload[0].name == "project-1"
        payload[0].nested.name == "nested-1"
        payload[19].name == "project-20"

        when: "verify job.data (job results)"
        def results = parseJsonBytes(job.data)
        then:
        results != null
        results instanceof List
        results.size() == 20
        results[0].ok == true
        results[0].status == HttpStatus.CREATED.value()
        results[10].ok == true

        and: "verify includes"
        results[0].data.size() == 3 //id, project name, nested name
        results[0].data.id == 1
        results[0].data.name == "project-1"
        results[0].data.nested.name == "nested-1"

        and: "Verify database records"
        Project.count() == 20
        Project.get(1).name == "project-1"
        Project.get(1).nested.name == "nested-1"
        Project.get(20).name == "project-20"
    }

    void "test bulk update"() {
        List list = generateDataList(10)

        when: "insert records"
        Long jobId = projectRepo.bulk(list, setupBulkableArgs())
        def job = TestRepoSyncJob.get(jobId)

        then:
        job.state == SyncJobState.Finished

        and: "Verify db records"
        Project.count() == 10

        when: "Bulk update"
        list.eachWithIndex {it, idx ->
            it.name = "updated-${idx + 1}"
            it.id = idx + 1
        }

        jobId = projectRepo.bulk(list, setupBulkableArgs(DataOp.update))
        job = TestRepoSyncJob.get(jobId)

        then:
        noExceptionThrown()
        job != null
        job.data != null
        job.state == SyncJobState.Finished

        and: "Verify db records"
        Project.count() == 10
        //Project.get(1).name == "updated-1" XXX FIX, some how doesnt update in unit tests
        //Project.get(10).name == "updated-10"

    }

    void "test failures and errors"() {
        given:
        List list = generateDataList(20)

        and: "Add few bad records"
        list[9].name = null
        list[19].nested.name = ""

        when: "bulk insert"
        Long jobId = projectRepo.bulk(list, setupBulkableArgs())
        def job = TestRepoSyncJob.get(jobId)

        then: "verify job"
        job.ok == false

        when: "verify job.data"
        def results = parseJsonBytes(job.data)

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
        results[9].data.nested.name == "nested-10"
        results[9].status == HttpStatus.UNPROCESSABLE_ENTITY.value()

        //results[9].title != null
        results[9].errors.size() == 1
        results[9].errors[0].field == "name"
        //results[9].errors[0].message == ""

        results[19].errors.size() == 1
        results[19].errors[0].field == "nested.name"
        //results[19].errors[0].field == "name"
    }


    @Issue("domain9#413")
    void "test batching"() {
        setup: "Set batchSize of 10 to trigger batching/slicing"
        asyncService.sliceSize = 10
        List<Map> list = generateDataList(60) //this should trigger 6 batches of 10

        when: "bulk insert in multi batches"
        Long jobId = projectRepo.bulk(list, setupBulkableArgs())
        def job = TestRepoSyncJob.findById(jobId)

        def results = parseJsonBytes(job.data)

        then: "just 60 should have been inserted, not the entire list twice"
        results.size() == 60

        cleanup:
        asyncService.sliceSize = 50
    }

    private List<Map> generateDataList(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            Map nested = [name: "nested-$index"]
            list << [name: "project-$index", testDate:"2021-01-01", isActive: false, nested: nested]
        }
        return list
    }

}
