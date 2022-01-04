package gorm.tools.repository

import gorm.tools.job.SyncJobState
import org.apache.commons.lang3.StringUtils
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.repository.bulk.BulkableArgs
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification
import yakworks.rally.job.SyncJob
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo

import static yakworks.commons.json.JsonEngine.parseJson

@Integration
@Rollback
class BulkableRepoIntegrationSpec extends Specification implements DomainIntTest {

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
        SyncJob job = SyncJob.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())

        then:
        json != null
        json.size() == 3
    }

    void "sanity check bulk update"() {
        given:
        List<Map> jsonList = generateOrgData(5)

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        SyncJob job = SyncJob.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "bulk update"
        def results = parseJson(job.dataToString())
        jsonList.eachWithIndex { it, idx ->
            it["id"] = results[idx].data.id
            it["comments"] = "flubber${it.id}"
        }

        jobId = orgRepo.bulk(jsonList, BulkableArgs.update(asyncEnabled: false))
        job = SyncJob.get(jobId)
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

    @Issue("domain9/issues/629")
    void "when lazy association encountered during json building"() {
        given:
        Org org
        Org.withNewTransaction {
            org = Org.create("testorg-1", "testorg-1", OrgType.Customer).persist()
        }

        flushAndClear()
        List<Map> contactData = [[org:[id: org.id], street1: "street1", street2: "street2", city: "city", state:"IN"]]

        when:
        BulkableArgs args = BulkableArgs.create(asyncEnabled: false)

        //include field from org, here org would be a lazy association, and would fail when its property accessed during json building
        args.includes = ["id", "org.source.id"]
        Long jobId = Location.repo.bulk(contactData, args)

        then:
        noExceptionThrown()
        jobId != null

        when:
        SyncJob job = SyncJob.get(jobId)

        then:
        job != null
        job.state == SyncJobState.Running //XX this should be Finished, but because json conversion failed, the job is never updated.

        when:
        List json = parseJson(job.dataToString())

        then:
        json != null
        json.size() > 0 //job.data has not been updated because json building failed
    }

    void "test failures should rollback"() {
        List<Map> jsonList = generateOrgData(1)
        jsonList[0].num = StringUtils.rightPad("ORG-1-", 110, "X")

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        SyncJob job = SyncJob.get(jobId)
        flush()

        then:
        noExceptionThrown()
        job.data != null

        when:
        List json = parseJson(job.dataToString())
        List requestData = parseJson(job.requestDataToString())

        then:
        json != null
        requestData != null

        and: "no dangling records committed"
        OrgSource.findBySourceIdLike("ORG-1%") == null
    }


    @Issue("#357")
    @Ignore //XXX FIx, started failing when parallel disabled.
    void "test spin back through failures and run them one by one"() {
        OrgSource os1, os2, os3

        setup:
        int sliceSize = orgRepo.parallelTools.asyncService.sliceSize
        orgRepo.parallelTools.asyncService.sliceSize = 10 //trigger batching

        and: "data bad contact records which would fail"
        List<Map> jsonList = generateOrgData(20)
        jsonList[5].contact = [name:""]
        jsonList[15].contact = [name:""]

        when:
        Long jobId = orgRepo.bulk(jsonList, BulkableArgs.create(asyncEnabled: false))
        flush()

        SyncJob job = SyncJob.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())

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
        SyncJob job = SyncJob.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())

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
