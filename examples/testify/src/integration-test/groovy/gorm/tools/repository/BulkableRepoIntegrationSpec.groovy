package gorm.tools.repository

import gorm.tools.repository.bulk.BulkableArgs
import gorm.tools.repository.bulk.BulkableRepo
import grails.converters.JSON
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import groovy.json.JsonSlurper
import org.grails.web.json.JSONArray
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource

@Integration
@Rollback
class BulkableRepoIntegrationSpec extends Specification {
    JdbcTemplate jdbcTemplate

    def cleanup() {
        //cleanup all orgs which would have been committed during tests because of parallel/async
        Org.withNewTransaction {
            Org.query(num:[$like:"testorg-%"]).deleteAll()
            OrgSource.where({ sourceId ==~ "testorg-%"}).deleteAll()
        }
    }

    void "sanity check bulk create"() {
        given:
        List<Map> jsonList = generateOrgData(3)

        when:
        Job job = ((BulkableRepo) Org.repo).bulk(jsonList, BulkableArgs.create())

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.data, "UTF-8"))

        then:
        json != null
        json.length() == 3
    }

    void "sanity check bulk update"() {
        given:
        List<Map> jsonList = generateOrgData(5)

        when:
        Job job = ((BulkableRepo) Org.repo).bulk(jsonList, BulkableArgs.create())

        then:
        noExceptionThrown()
        job.data != null

        when: "bulk update"
        def results = toJson(job.data)
        jsonList.eachWithIndex { it, idx ->
            it["id"] = results[idx].data.id
            it["comments"] = "comment-${it.id}"
        }

        job = ((BulkableRepo) Org.repo).bulk(jsonList, BulkableArgs.update())

        then:
        noExceptionThrown()
        job != null

        when: "Verify updated records"
        int count
        Org.withNewTransaction {
            count = Org.countByCommentsIlike("comment-%")
        }
        then:
        count == 5
    }

    @Issue("#357")
    void "test spin back through failures and run them one by one"() {
        OrgSource os1, os2, os3

        setup:
        int sliceSize = ((BulkableRepo) Org.repo).parallelTools.sliceSize
        ((BulkableRepo) Org.repo).parallelTools.sliceSize = 10 //trigger batching

        and: "data bad contact records which would fail"
        List<Map> jsonList = generateOrgData(20)
        jsonList[5].contact = [name:"xxxx"]
        jsonList[15].contact = [name:"xxxx"]

        when:
        Job job = ((BulkableRepo) Org.repo).bulk(jsonList, BulkableArgs.create())

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.data, "UTF-8"))

        then: "job is good"
        json != null
        json.length() == 20

        and: "just 2 records should have failed, all other should be successfull"
        json.count({it.ok == false}) == 2 //

        and: "Verify bad records"
        json[5].ok == false
        json[15].ok == false
        json[5].errors != null

        when:
        Org.withNewTransaction {
            os1 = OrgSource.findBySourceId(jsonList[5].num) //this should have rolled back when contact save fails
            os2 = OrgSource.findBySourceId(jsonList[15].num)
            os3 = OrgSource.findBySourceId("testorg-1") //this should exist
        }

        then: "Verify no dangling records have been commited"
        os1 == null
        os2 == null
        os3 != null

        cleanup: "Cleanup orgs as they would have been committed during bulk"
        ((BulkableRepo) Org.repo).parallelTools.sliceSize = sliceSize //set original back
    }

    @Ignore("Fix XXX in BulkableRepo")
    void "test data access exception on db constraint violation during flush"() {
        setup:
        Org.withNewTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX org_num_unique ON Org(num)")
        }

        List<Map> jsonList = generateOrgData(3)
        jsonList[2].num = "testorg-2"

        when:
        Job job = ((BulkableRepo)Org.repo).bulk(jsonList, BulkableArgs.create())

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        JSONArray json = JSON.parse(new String(job.data, "UTF-8"))

        then:
        json != null
        json.length() == 3
        json.ok == true

        cleanup:
        Org.withNewTransaction {
            jdbcTemplate.execute("DROP index org_num_unique")
        }
    }

    private List<Map> generateOrgData(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            Map info = [phone: "p-$index"]
            list << [num:"testorg-$index", name: "org-$index", info: info, type:"Customer"]
        }
        return list
    }

    def toJson(byte[] data) {
        def slurper = new JsonSlurper()
        return slurper.parse(data)
    }
}
