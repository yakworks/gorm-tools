package yakworks.rally.job

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.job.DataLayout
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.etl.DataMimeTypes
import yakworks.gorm.api.bulk.BulkImportJobArgs
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.json.groovy.JsonEngine
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo

import static yakworks.json.groovy.JsonEngine.parseJson

@Integration
@Rollback
class BulkImportTests extends Specification implements DomainIntTest {

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

    BulkImportJobArgs setupBulkImportParams(DataOp op = DataOp.add){
        return new BulkImportJobArgs(
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


    void "queue job - payload with null key values"() {
        setup:
        List payload = [[id:1, flex:[text1:null, text2: "test"]]]
        BulkImportJobArgs bulkImportJobArgs = BulkImportJobArgs.fromParams(
            sourceId: '123', source: 'some source',
            'foo': 'bar'
        )
        bulkImportJobArgs.entityClassName = Org.name
        var bulkImportService = BulkImportService.lookup(Org)
        SyncJobEntity job = bulkImportService.queueJob(bulkImportJobArgs, payload)
        flushAndClear()

        expect:
        job

        when:
        job = SyncJob.get(job.id)
        def payloadList = JsonEngine.parseJson(job.payloadToString(), List<Map>)

        then:
        payloadList
        payloadList.size() == 1
        payloadList[0].flex
        payloadList[0].flex.text2 == "test"
        payloadList[0].flex.containsKey("text1")
        payloadList[0].flex.text1 == null

    }

    void "test queueJob and save payload to file large payload"() {
        when:
        List payload = []
        //puts it past the 1000 count so will get saved as attachment and have a payloadId
        (1..1001).each {
            payload << it
        }


        BulkImportJobArgs bulkImportJobArgs = BulkImportJobArgs.fromParams(
            sourceId: '123', source: 'some source',
            'foo': 'bar'
        )
        bulkImportJobArgs.entityClassName = Org.name
        var bulkImportService = BulkImportService.lookup(Org)
        SyncJobEntity job = bulkImportService.queueJob(bulkImportJobArgs, payload)

        then:
        noExceptionThrown()

        when:
        flushAndClear()

        job = SyncJob.get(job.id)

        then:
        noExceptionThrown()
        job.id
        job.state == SyncJobState.Queued
        job.id
        job.jobType == 'bulk.import'
        job.source == 'some source'
        job.sourceId == '123'
        job.payloadId
        Attachment.get(job.payloadId).name.startsWith("Sync")
        //check params
       //Keys [sourceId, dataFormat, dataLayout, payloadId, entityClassName, jobType, source, foo]
        job.params.keySet() == [
            'jobType', 'source', 'sourceId', 'dataFormat', 'payloadFormat', 'entityClassName',
            'payloadId', 'dataLayout', 'foo', 'async'
        ] as Set

        job.params['foo']

        cleanup:
        if(job.payloadId) Attachment.repo.removeById(job.payloadId)
    }

    void "test queueJob with attachment id"() {
        when:

        BulkImportJobArgs bulkImportJobArgs = BulkImportJobArgs.fromParams(
            sourceId: '123', source: 'some source',
            attachmentId: 123,
            'foo': 'bar'
        )

        var bulkImportService = BulkImportService.lookup(Org)
        SyncJobEntity job = bulkImportService.queueJob(bulkImportJobArgs, null)
        flushAndClear()

        then:
        noExceptionThrown()

        when:
        job = SyncJob.get(job.id)

        then:
        noExceptionThrown()
        job.id
        job.state == SyncJobState.Queued
        job.id
        job.jobType == 'bulk.import'
        job.source == 'some source'
        job.sourceId == '123'
        job.payloadId == 123

        //check params
        //Keys [sourceId, dataFormat, dataLayout, payloadId, entityClassName, jobType, source, foo]
        job.params.keySet() == [
            'jobType',
            'source',
            'sourceId',
            'dataFormat',
            'dataLayout',
            'payloadId',
            'payloadFormat',
            'entityClassName',
            'attachmentId',
            'async',
            'foo',
        ] as Set

        job.params['foo']

    }

    void "test BulkImportJobArgs from SyncJob"() {
        when:

        BulkImportJobArgs bulkImportJobArgs = BulkImportJobArgs.fromParams(
            sourceId: '123',
            source: 'some source',
            attachmentId: 123,
            headerPathDelimiter: '_',
            'foo': 'bar'
        )

        var bulkImportService = BulkImportService.lookup(Org)
        SyncJobEntity job = bulkImportService.queueJob(bulkImportJobArgs, null)
        flushAndClear()

        then:
        noExceptionThrown()

        when:
        job = SyncJob.get(job.id)
        BulkImportJobArgs jobArgs = bulkImportService.setupJobArgs(job)

        then:
        noExceptionThrown()
        jobArgs.jobId == job.id
        jobArgs.jobType == 'bulk.import'
        jobArgs.source == 'some source'
        jobArgs.sourceId == '123'
        jobArgs.payloadId == 123
        jobArgs.attachmentId == 123
        jobArgs.headerPathDelimiter == '_'
        jobArgs.entityClassName == 'yakworks.rally.orgs.model.Org'
        jobArgs.entityClass == Org
        jobArgs.queryParams['foo'] == 'bar'
        jobArgs.includes == ['*']
        jobArgs.dataFormat == DataMimeTypes.json
        jobArgs.dataLayout == DataLayout.Result

        //check jobArgs.queryParams
        //Keys [sourceId, dataFormat, dataLayout, payloadId, entityClassName, jobType, source, foo]
        jobArgs.queryParams.keySet() == [
            'jobType', 'source', 'sourceId', 'async',
            'dataFormat', 'payloadFormat',
            'entityClassName', 'payloadId', 'attachmentId',
            'dataLayout', 'headerPathDelimiter', 'foo'
        ] as Set
        //
        // job.params['foo']

    }

    void "sanity check bulk create"() {
        given:
        List<Map> jsonList = generateOrgData(3)

        when:
        def impParams = new BulkImportJobArgs(parallel: false, async:false, op: DataOp.add)
        //XXX @SUD make all the tests in this class work with new way
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        assert job.state == SyncJobState.Finished
        List json = job.dataList

        then:
        json
        json.size() == 3

        when: "verify syncjob events"
        Org org = Org.findByNum("testorg-1")

        then:
        org != null
        org.info != null
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
        job.dataToString()

        when: "bulk update"
        def results = job.dataList
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

        def impParams = new BulkImportJobArgs(
            parallel: false, async:false, op: DataOp.add,
            //include field from org, here org would be a lazy association, and would fail when its property accessed during json building
            includes: ["id", "org.source.id"]
        )
        def job = getBulkImportService(Location).bulkImportLegacy(impParams, contactData)

        then:
        job != null
        job.state == SyncJobState.Finished

        when:
        List json = job.dataList

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
        job.dataToString()

        when:
        List json = job.dataList
        List requestData = job.payloadList

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

        def results = job.dataList

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
        def impParams = new BulkImportJobArgs(
            parallel: true, async:false, op: DataOp.add
        )
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        // Long jobId = getBulkImportService(Org).bulkLegacy(jsonList, SyncJobArgs.create(async:false))
        // def job = SyncJob.repo.getWithTrx(jobId)

        then:
        noExceptionThrown()
        job.state == SyncJobState.Finished
        job.dataToString() != null

        when: "verify json"
        List json = job.dataList
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
        def impParams = new BulkImportJobArgs(
            parallel: true, async:false, op: DataOp.add
        )
        def job = getBulkImportService(Org).bulkImportLegacy(impParams, jsonList)

        then:
        noExceptionThrown()
        job.state == SyncJobState.Finished
        job.dataToString() != null

        when: "verify json"
        List json = job.dataList

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
