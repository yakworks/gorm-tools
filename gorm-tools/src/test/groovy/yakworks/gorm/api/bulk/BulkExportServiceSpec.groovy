package yakworks.gorm.api.bulk

import gorm.tools.beans.Pager
import gorm.tools.job.DataLayout
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import testing.TestSyncJob
import testing.TestSyncJobService
import yakworks.api.problem.data.DataProblemException
import yakworks.meta.MetaMapList
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class BulkExportServiceSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, SinkItem, TestSyncJob]
    static springBeans = [TestSyncJobService]

    // @Transactional
    void setupSpec() {
        KitchenSink.createKitchenSinks(300)
    }

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(
            parallel: false, async:false, op: op, jobType: BulkImportJobArgs.JOB_TYPE,
            source: "test", sourceId: "test", includes: ["id", "name", "ext.name"]
        )
    }

    BulkExportService<KitchenSink> getBulkExportService(){
        BulkExportService.lookup(KitchenSink)
    }

    void "test setupJobArgs"() {
        given:
        SyncJobEntity jobEnt = bulkExportService.queueJob(
            new BulkExportJobArgs(
                q: '{"foo": "bar"}',
                includes: ["id", "name", "ext.name"],
                sourceId: "test-job"
            )
        )

        when:
        BulkExportJobArgs jobArgs = bulkExportService.setupJobArgs(jobEnt)

        then:
        noExceptionThrown()
        jobArgs
        jobArgs.jobType == BulkExportJobArgs.JOB_TYPE
        jobArgs.sourceId == "test-job"
        jobArgs.queryArgs
        jobArgs.entityClass == KitchenSink
        jobArgs.includes == ['id','name','ext.name']
        jobArgs.dataLayout == DataLayout.List
    }

    Long bulkExport(String q){
        // Map params = [
        //     parallel: false, async:false,
        //     source: "test", sourceId: "test-job", includes: "id,name,ext.name"
        // ]
        // params.q = q
        BulkExportJobArgs bexParams = new BulkExportJobArgs(
            sourceId: "test-job", includes: ['id','name','ext.name'],
            q: q
        )

        SyncJobEntity jobEnt = bulkExportService.queueJob(bexParams)
        //flushAndClear()
        SyncJobEntity jobEnt2 = bulkExportService.runJob(jobEnt.id)
        //flushAndClear()
        jobEnt.id
    }

    void "test queueExportJob"() {
        when:
        SyncJobEntity jobEnt = bulkExportService.queueJob(
            new BulkExportJobArgs(
                q: '{"foo": "bar"}',
                includes: ["id", "name", "ext.name"],
                sourceId: "test-job"
            )
        )
        flushAndClear()
        assert jobEnt.id

        def job = TestSyncJob.get(jobEnt.id)
        Map params = job.params

        then:
        noExceptionThrown()
        job.jobType == BulkExportJobArgs.JOB_TYPE
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'

        and: 'params have extra fields'
        params.q == '{"foo": "bar"}'
        params.entityClassName == 'yakworks.testing.gorm.model.KitchenSink'

    }

    void "test empty q param"() {
        when:
        Map params = [:]
        SyncJobEntity jobEnt = bulkExportService.queueJob(new BulkExportJobArgs())

        then:
        DataProblemException ex = thrown()
        ex.code == 'error.query.qRequired'
    }

    void "test eachPage"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$gte":1}}'
        )
        def syncjobArgs =  new SyncJobArgs(
            sourceId: "test", includes: ["id", "name", "ext.name"],
            queryArgs: queryArgs
        )
        var ctx = SyncJobContext.of(syncjobArgs)

        List dataList = []
        bulkExportService.eachPage(ctx){ List dataPage ->
            dataList.addAll(dataPage)
        }

        List sortedList = dataList.sort {
            it.id
        }
        //make sure paging is good and they are all different"
        Long lastId = 0
        sortedList.each {
            assert it.id > lastId
            lastId = it.id
        }

        then:
        sortedList.size() == 300
    }

    void "test setupPager"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$lte":100}}'
        )
        def syncjobArgs =  new SyncJobArgs(
            sourceId: "test", includes: ["id", "name", "ext.name"],
            queryArgs: queryArgs
        )
        var ctx = SyncJobContext.of(syncjobArgs)

        Pager pager = bulkExportService.setupPager(ctx)

        then:
        pager.recordCount ==  KitchenSink.query(id:['$lte':100]).count()
        pager.max == bulkExportService.pageSize
        pager.offset == 0
        ctx.payloadSize == 100
    }

    void "test eachPage with no data"() {
        when:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$lt":1}}'
        )
        def syncjobArgs =  new SyncJobArgs(
            sourceId: "test", includes: ["id", "name", "ext.name"],
            queryArgs: queryArgs
        )
        var ctx = SyncJobContext.of(syncjobArgs)

        List dataList = []
        bulkExportService.eachPage(ctx){ List dataPage ->
            dataList.addAll(dataPage)
        }

        then:
        dataList.size() == 0
    }

    void "runPageQuery"() {
        setup:
        QueryArgs queryArgs = QueryArgs.of(
            q: '{"id":{"$gt":1}}'
        )
        def syncjobArgs =  new SyncJobArgs(
            sourceId: "test", includes: ["id", "name", "ext.name"],
            queryArgs: queryArgs
        )
        var ctx = SyncJobContext.of(syncjobArgs)
        Pager pager = bulkExportService.setupPager(ctx)

        when:
        MetaMapList list = bulkExportService.runPageQuery(syncjobArgs, pager)

        then:
        list.totalCount == KitchenSink.query(id:['$gt':1]).count()
        list
        list[0].keySet().containsAll(['id', 'name', 'ext'])
    }

}
