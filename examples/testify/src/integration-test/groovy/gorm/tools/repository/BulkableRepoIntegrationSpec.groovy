package gorm.tools.repository

import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.json.JsonParserTrait
import gorm.tools.repository.bulk.BulkableArgs
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.job.Job
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.gorm.testing.DomainIntTest

@Integration
@Rollback
class BulkableRepoIntegrationSpec extends Specification implements DomainIntTest, JsonParserTrait {

    JdbcTemplate jdbcTemplate
    OrgRepo orgRepo

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
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        Job job = Job.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJsonBytes(job.data)

        then:
        json != null
        json.size() == 3
    }

    void "sanity check bulk update"() {
        given:
        List<Map> jsonList = generateOrgData(5)

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        Job job = Job.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "bulk update"
        def results = parseJsonBytes(job.data)
        jsonList.eachWithIndex { it, idx ->
            it["id"] = results[idx].data.id
            it["comments"] = "flubber${it.id}"
        }

        jobId = orgRepo.bulk(jsonList, BulkableArgs.update(asyncEnabled: false))
        job = Job.get(jobId)
        flushAndClear()

        then:
        noExceptionThrown()
        job != null

        when: "Verify updated records"
        def listUp = Org.query(comments: "flubber%").list()
        int count = Org.countByCommentsLike("flubber%")
        // Org.withNewTransaction {
        //     count = Org.countByCommentsIlike("comment-%")
        // }
        then:
        count == 5
    }

    void "test failures should rollback"() {
        List<Map> jsonList = generateOrgData(1)
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "X")

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        Job job = Job.get(jobId)
        flush()

        then:
        noExceptionThrown()
        job.data != null

        when:
        List json = parseJsonBytes(job.data)
        List requestData = parseJsonBytes(job.requestData)

        then:
        json != null
        requestData != null

        and: "no dangling records committed"
        OrgSource.findBySourceIdLike("ORG-1%") == null
    }


    @Issue("#357")
    void "test spin back through failures and run them one by one"() {
        OrgSource os1, os2, os3

        setup:
        int sliceSize = orgRepo.parallelTools.asyncService.sliceSize
        orgRepo.parallelTools.asyncService.sliceSize = 10 //trigger batching

        and: "data bad contact records which would fail"
        List<Map> jsonList = generateOrgData(20)
        jsonList[5].contact = [name:"xxxx"]
        jsonList[15].contact = [name:"xxxx"]

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        Job job = Job.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJsonBytes(job.data)

        then: "job is good"
        json != null
        json.size() == 20

        and: "just 2 records should have failed, all other should be successfull"
        json.count({it.ok == false}) == 2 //

        and: "Verify bad records"
        json[5].ok == false
        json[15].ok == false
        json[5].errors != null

        when:
        // Org.withNewTransaction {
            os1 = OrgSource.findBySourceId(jsonList[5].num) //this should have rolled back when contact save fails
            os2 = OrgSource.findBySourceId(jsonList[15].num)
            os3 = OrgSource.findBySourceId("testorg-1") //this should exist
        // }

        then: "Verify no dangling records have been commited"
        os1 == null
        os2 == null
        os3 != null

        cleanup: "Cleanup orgs as they would have been committed during bulk"
        orgRepo.parallelTools.asyncService.sliceSize = sliceSize //set original back
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
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create())
        Job job = Job.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJsonBytes(job.data)

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

}
