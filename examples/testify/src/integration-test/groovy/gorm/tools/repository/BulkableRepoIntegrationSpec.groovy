package gorm.tools.repository

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.IgnoreRest
import spock.lang.Issue
import spock.lang.Specification
import yakworks.gorm.testing.model.KitchenSink
import yakworks.rally.job.SyncJob
import yakworks.gorm.testing.DomainIntTest
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo

import static yakworks.commons.json.JsonEngine.parseJson

@Integration
// @Rollback
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

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(asyncEnabled: false, op: op, source: "test", sourceId: "test",
            includes: ["id", "name", "ext.name"])
    }

    SyncJob getJob(Long jobId){
        withNewTrx {
            return SyncJob.get(jobId)
        }
    }

    void "sanity check bulk create"() {
        given:
        List<Map> jsonList = generateOrgData(3)

        when:
        Long jobId = orgRepo.bulk(jsonList, SyncJobArgs.create(asyncEnabled: false))
        SyncJob job = getJob(jobId) //= SyncJob.repo.read(jobId)

        List json = parseJson(job.dataToString())

        then:
        json
        json.size() == 3
    }

    void "sanity check bulk update"() {
        given:
        List<Map> jsonList = generateOrgData(5)

        when:
        Long jobId = orgRepo.bulk(jsonList, SyncJobArgs.create(asyncEnabled: false))
        SyncJob job = getJob(jobId) //SyncJob.get(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "bulk update"
        def results = parseJson(job.dataToString())
        // assert results[0].data == jsonList
        //update jsonList to prepare for a bulUpdate
        jsonList.eachWithIndex { it, idx ->
            it["id"] = results[idx].data.id
            it["comments"] = "flubber${it.id}"
        }

        jobId = orgRepo.bulk(jsonList, SyncJobArgs.update(asyncEnabled: false))
        job = getJob(jobId) //SyncJob.get(jobId)
        // flushAndClear()

        then:
        noExceptionThrown()
        job != null

        when: "Verify updated records"
        int count

        Org.withTransaction {
            def listUp = Org.query(comments: "flubber%").list()
            count = Org.countByCommentsLike("flubber%")
        }
        then:
        count == 5
    }

    @Ignore //XXX fix by generating json on demand in session
    @Issue("domain9/issues/629")
    void "when lazy association encountered during json building"() {
        given:
        Org org = Org.create("testorg-1", "testorg-1", OrgType.Customer).persist()
        // Org.withTransaction {
        //     org = Org.create("testorg-1", "testorg-1", OrgType.Customer).persist()
        // }

        flushAndClear()
        List<Map> contactData = [[org:[id: org.id], street1: "street1", street2: "street2", city: "city", state:"IN"]]

        when:
        SyncJobArgs args = SyncJobArgs.create(asyncEnabled: false)

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
        Long jobId = orgRepo.bulk(jsonList, SyncJobArgs.create(asyncEnabled: false))
        SyncJob job = SyncJob.repo.getWithTrx(jobId)
        flush()

        then:
        noExceptionThrown()
        job.data != null

        when:
        List json = parseJson(job.dataToString())
        List requestData = parseJson(job.payloadToString())

        then:
        json != null
        requestData != null

        and: "no dangling records committed"
        Org.withTransaction {
            OrgSource.findBySourceIdLike("ORG-1%") == null
        }

    }



    void "test failures and errors"() {
        given:
        List list = KitchenSink.generateDataList(20)

        and: "Add a bad records"
        list[1].ext.name = null
        // list[19].ext.name = null

        when: "bulk insert"

        Long jobId = KitchenSink.repo.bulk(list, setupSyncJobArgs())
        def job = SyncJob.repo.getWithTrx(jobId)

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

        results[1].errors.size() == 1
        results[1].errors[0].field == "ext.name"
    }


    void "test data access exception on db constraint violation during flush"() {
        setup:
        Org.withNewTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX org_num_unique ON Org(num)")
        }

        List<Map> jsonList = generateOrgData(2)
        //change second item in array to same as first
        jsonList[1].num = "testorg-1"

        when:
        Long jobId = orgRepo.bulk(jsonList, SyncJobArgs.create())
        SyncJob job = SyncJob.repo.getWithTrx(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())

        then:
        json != null
        json.size() == 2
        json[1].ok == false

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
