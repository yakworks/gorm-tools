package gorm.tools.repository

import gorm.tools.job.JobState
import gorm.tools.repository.bulk.BulkableRepo
import gorm.tools.testing.unit.DataRepoTest
import groovy.json.JsonSlurper
import org.springframework.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import testing.JobImpl
import testing.Nested
import testing.Project

class BulkableRepoSpec extends Specification implements DataRepoTest {

    @Shared JsonSlurper slurper

    void setupSpec() {
        slurper = new JsonSlurper()
        mockDomains(Project, Nested, JobImpl)
    }

    void "test bulkable repo"() {
        expect:
        Project.repo instanceof BulkableRepo
    }

    void "test bulk insert success"() {
        given:
        List list = generateDataList(20)

        when: "bulk insert 20 records"
        JobImpl job = Project.repo.bulkCreate(list, [source:"test", sourceId: "test", includes: ["id", "name", "nested.name"]])

        then: "verify job"
        job != null
        job.source == "test"
        job.sourceId == "test"
        job.data != null
        job.results != null
        job.state == JobState.Finished

        when: "Verify job.data"
        def payload = toJson(job.data)

        then:
        payload != null
        payload instanceof List
        payload.size() == 20
        payload[0].name == "project-1"
        payload[0].nested.name == "nested-1"
        payload[19].name == "project-20"

        when: "verify job.results"
        def results = toJson(job.results)
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

    void "test failures and errors"() {
        given:
        List list = generateDataList(20)

        and: "Add few bad records"
        list[9].name = null
        list[19].nested.name = ""

        when: "bulk insert"
        JobImpl job = Project.repo.bulkCreate(list, [source:"test", sourceId: "test", includes: ["id", "name", "nested.name"]])

        then: "verify job"
        job.ok == false

        when: "verify job.results"
        def results = toJson(job.results)

        then:
        results != null
        results instanceof List

        and: "verify successfull results"
        results.findAll({ it.ok == true}).size() == 18
        results[0].ok == true

        and: "Failed records"
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

    private List<Map> generateDataList(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            Map nested = [name: "nested-$index"]
            list << [name: "project-$index", testDate:"2021-01-01", isActive: false, nested: nested]
        }
        return list
    }

    def toJson(byte[] data) {
        return slurper.parse(data)
    }


}