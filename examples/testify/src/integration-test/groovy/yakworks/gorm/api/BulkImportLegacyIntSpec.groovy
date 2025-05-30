package yakworks.gorm.api

import yakworks.gorm.api.bulk.BulkImportJobParams
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import grails.gorm.transactions.NotTransactional
import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class BulkImportLegacyIntSpec extends Specification implements DomainIntTest {

    JdbcTemplate jdbcTemplate
    OrgRepo orgRepo
    KitchenSinkRepo kitchenSinkRepo

    def cleanup() {
        //cleanup all orgs which would have been committed during tests because of parallel/async
        Org.withNewTransaction {
            Org.query(num:[$like:"testorg-%"]).deleteAll()
            OrgSource.where({ sourceId ==~ "testorg-%"}).deleteAll()
        }
    }

    public <D> BulkImportService<D> getBulkImportService(Class<D> entClass){
        BulkImportService.lookup(entClass)
    }

    BulkImportJobParams setupBulkImportParams(DataOp op = DataOp.add){
        return new BulkImportJobParams(
            op: op, parallel: false, async:false,
            source: "test", sourceId: "test",
            includes: ["id", "name", "ext.name"]
        )
    }

    SyncJob getJob(Long jobId){
        SyncJob.repo.clear() //make sure session doesn't have it cached
        return SyncJob.get(jobId)
        // withNewTrx {
        //     return SyncJob.get(jobId)
        // }
    }

    void "sanity check bulk create"() {
        given:
        List<Map> jsonList = generateOrgData(3)

        when:
        def impParams = new BulkImportJobParams(parallel: false, async:false, op: DataOp.add)
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        assert job.state == SyncJobState.Finished
        List json = parseJson(job.dataToString())

        then:
        json
        json.size() == 3

        when: "verify syncjob events"
        Org org = Org.findByNum("testorg-1")

        then:
        org != null
        org.comments != null
        //verify the SyncjobEventListener registered properly
        org.comments == "testorg-1-BulkImportFinishedEvent"
        org.info != null
        //verify the SyncjobEventListener registered properly
        org.info.fax == "SyncJobStateEvent"
        //verify the Before and AfterCreateOrUpdate
        org.flex.text9 == 'from before'
        org.flex.text10 == 'from after'

    }

    @NotTransactional
    void "force error in KitchenSink slice"() {
        setup:
        //make unique index and force a slice to fail
        KitchenSink.withNewTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX sink_num_unique ON KitchenSink(num)")
        }

        List list = KitchenSink.generateDataList(300, [comments: 'GoDogGo'])
        list.eachWithIndex {Map it, int index ->
            it["num"] = "num$index"
        }
        when:
        // force a failuer on index, bulk does 2 passes,  fails entire slice in first pass
        // then it goes through and runs one by one for data
        // kitchenSinkRepo removes the key for name2, so this will also verify that its sending a clone
        // so that second pass uses original data
        list[5].num ="num1" //will fail (During flush) on this one as it already exists

        //Long jobId = getBulkImportService(KitchenSink).bulkLegacy(list, setupSyncJobArgs())
        def job = getBulkImportService(KitchenSink).bulkImportLegacy(setupBulkImportParams(), list)

        def dbData, successCount

        KitchenSink.withNewTransaction {
            //job = SyncJob.get(jobId)
            dbData = KitchenSink.findAllWhere(comments: 'GoDogGo')
            successCount = KitchenSink.countByComments("GoDogGo") //All except 1 should have been inserted
        }

        then: "verify job"
        job.state == SyncJobState.Finished
        //first slice will have run through second time, make sure its good
        successCount == 299
        dbData[0].name2 != null
        dbData[2].name2 != null

        cleanup:
        KitchenSink.withNewTransaction {
            jdbcTemplate.execute("DROP index sink_num_unique")
            KitchenSink.query(comments:"GoDogGo").deleteAll()
        }
    }

    void "sanity check bulk update"() {
        given:
        List<Map> jsonList = generateOrgData(5)

        when:

        def job = getBulkImportService(Org).bulkImportLegacy(setupBulkImportParams(), jsonList)

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

        job = getBulkImportService(Org).bulkImportLegacy(setupBulkImportParams(DataOp.update), jsonList)

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

    void "when lazy association encountered during json building"() {
        given:
        Org org = Org.of("testorg-1", "testorg-1", OrgType.Customer).persist()
        // Org.withTransaction {
        //     org = Org.create("testorg-1", "testorg-1", OrgType.Customer).persist()
        // }

        flushAndClear()
        List<Map> contactData = [[org:[id: org.id], street1: "street1", street2: "street2", city: "city", state:"IN"]]

        when:

        def impParams = new BulkImportJobParams(
            parallel: false, async:false, op: DataOp.add,
            //include field from org, here org would be a lazy association, and would fail when its property accessed during json building
            includes: ["id", "org.source.id"]
        )
        def job = getBulkImportService(Location).bulkImportLegacy(impParams, contactData)

        then:
        job != null
        job.state == SyncJobState.Finished

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
        def job = getBulkImportService(Org).bulkImportLegacy(setupBulkImportParams(), jsonList)

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

        def job = getBulkImportService(KitchenSink).bulkImportLegacy(setupBulkImportParams(), list)

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

    void "DataAccessException rollback slice but should process the rest"() {
        setup:
        Org.withNewTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX org_num_unique ON Org(num)")
        }

        List<Map> jsonList = generateOrgData(300)
        //change a item in array so fails on unique num
        jsonList[1].num = "testorg-1"

        when:
        def impParams = new BulkImportJobParams(
            parallel: true, async:false, op: DataOp.add
        )
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        // Long jobId = getBulkImportService(Org).bulkLegacy(jsonList, SyncJobArgs.create(async:false))
        // def job = SyncJob.repo.getWithTrx(jobId)

        then:
        noExceptionThrown()
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())
        List jsonSuccess = json.findAll({ it.ok == true})
        List jsonFail = json.findAll({ it.ok == false})

        then:
        json.size() == 300
        jsonSuccess.size() == 299
        jsonFail.size() == 1

        OrgFlex.countByText1('goGoGadget') == 299

        when:
        Map failed =  json.find({ it.ok == false})

        then:
        failed != null
        failed.ok == false
        failed.code.contains "uniqueConstraintViolation"
        failed.title.contains "Unique index or primary key violation"
        !failed.containsKey("errors") //in this case violations would be empty, so errors field should not be present

        and: "Verify data is not empty"
        failed.data != null
        failed.data instanceof Map
        failed.data.num == "testorg-1"
        failed.data.name == "org-2"
        failed.data.info instanceof Map
        failed.data.flex instanceof Map

        cleanup:
        Org.withNewTransaction {
            jdbcTemplate.execute("DROP index org_num_unique")
        }
    }

    void "DataAccessException during processing slice errors"() {
        setup:
        Org.withNewTransaction {
            jdbcTemplate.execute("CREATE UNIQUE INDEX org_num_unique ON Org(num)")
        }

        List<Map> jsonList = generateOrgData(10)
        //change a item in array so fails on unique num
        jsonList[0].type = null //this one will fail the batch
        jsonList[3].num = "testorg-2" //this one should cause error when processing slice errors

        when:
        def impParams = new BulkImportJobParams(
            parallel: true, async:false, op: DataOp.add
        )
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        then:
        noExceptionThrown()
        job.state == SyncJobState.Finished
        job.data != null

        when: "verify json"
        List json = parseJson(job.dataToString())

        then:
        json != null
        json.findAll({ it.ok == true}).size() == 8

        when:
        Map failed =  json.find({ it.ok == false})

        then:
        failed != null
        failed.ok == false

        cleanup:
        Org.withNewTransaction {
            jdbcTemplate.execute("DROP index org_num_unique")
        }
    }

    //NOTE: SyncjobEventListener component messes with the data for tests
    private List<Map> generateOrgData(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            Map info = [phone: "p-$index"]
            list << [num:"testorg-$index", name: "org-$index", info: info, type:"Customer", flex:[text1: 'goGoGadget']]
        }
        return list
    }


}
