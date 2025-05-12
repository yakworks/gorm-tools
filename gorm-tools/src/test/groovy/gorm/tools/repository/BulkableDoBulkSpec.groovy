package gorm.tools.repository

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.problem.ValidationProblem
import gorm.tools.repository.bulk.BulkImporter
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import testing.Cust
import testing.TestSyncJob
import yakworks.api.ApiResults
import yakworks.api.HttpStatus
import yakworks.api.OkResult
import yakworks.spring.AppCtx
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * Tests for doBulk without the async and parallel stuff.
 * easier to test core logic
 */
class BulkableDoBulkSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkItem, SinkExt, TestSyncJob]
    // static springBeans = [syncJobService: TestSyncJobService]

    @Autowired KitchenSinkRepo kitchenSinkRepo

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(parallel: false, async:false, op: op, source: "test", sourceId: "test", includes: ["id", "name", "ext.name"])
    }

    BulkImporter getBulkImporter(){
        def bis = new BulkImporter(KitchenSink)
        AppCtx.autowire(bis)
        return bis
    }

    void "success doBulk add"() {
        given:
        List list = KitchenSink.generateDataList(100)
        def syncArgs = setupSyncJobArgs()

        when: "doBulk insert records"
        ApiResults res = bulkImporter.doBulkSlice(list, syncArgs)

        then: "verify"
        res.ok //all ok
        res.list.size() == 100
        // sanity check result payloads
        // payload comes from the buildSuccessMap
        res.list[0].ok

        when:
        def payload1 = res.list[0].payload

        then:"payload should be Map with includes from syncArgs"
        payload1 instanceof Map
        payload1.keySet() == ['id', 'name', 'ext'] as Set
        payload1.name == "Blue Cheese"
        payload1.ext.name == "SinkExt1"

        and: "spot check ok result"
        OkResult res1 = res.success[1]
        res1 instanceof OkResult
        res1.status == HttpStatus.CREATED //should be 201 CREATED

        when:
        def bcks
        KitchenSink.withTransaction {
            bcks = KitchenSink.findByName("Blue Cheese")
        }

        then: "check database records"

        bcks
        bcks.ext.name == "SinkExt1"
        KitchenSink.count() == 100
        KitchenSink.findByName("Oranges")
    }

    void "test bulk update"() {
        given:
        KitchenSink.createKitchenSinks(10)
        assert KitchenSink.count() == 10
        List updateList = KitchenSink.list().collect {
            [id: it.id, name: "new${it.id}"]
        }
        def syncArgs = setupSyncJobArgs(DataOp.update)

        when: "doBulk update records"
        ApiResults res = bulkImporter.doBulkSlice(updateList, syncArgs)

        then:
        res.ok
        res.success.size() == 10

        and: "spot check ok result"
        OkResult res1 = res.success[1]
        res1 instanceof OkResult
        res1.status == HttpStatus.OK //should be 200

        and: "verify data"
        KitchenSink.list().each {
            assert it.name == "new${it.id}"
        }
    }

    void "test failures and errors insert"() {
        given:
        List list = KitchenSink.generateDataList(3)
        def syncArgs = setupSyncJobArgs()

        and: "bad record with null name"
        list[1].name = null

        when: "bulk insert"
        ApiResults res = bulkImporter.doBulkSlice(list, syncArgs)

        then: "should have thrown exception"
        thrown(ValidationProblem.Exception)

        when: "bulk insert with trx per item"
        res = bulkImporter.doBulkSlice(list, syncArgs, true)

        then: "should have thrown exception"
        !res.ok //all ok
        res.success.size() == 2
        res.problems.size() == 1
        ValidationProblem problem = res.list[1]
        problem instanceof ValidationProblem
        //sanity check violation on propblem
        problem.violations[0].field == 'name'
        problem.violations[0].code == 'NotNull'
    }


    void "test bulk UPSERT"() {
        given:
        KitchenSink.createKitchenSinks(10)
        assert KitchenSink.count() == 10

        //change the existing ones
        List upsertList = KitchenSink.list().collect {
            [id: it.id, name: "updated${it.id}"]
        }
        //add in some news ones
        (901..910).each {
            upsertList.add(
                [id: it, num: it, name: "inserted${it}"]
            )
        }

        def syncArgs = setupSyncJobArgs(DataOp.upsert)
        //set bindId so it will work with ids in the find which normally throws error, just like insert
        syncArgs.persistArgs(bindId: true)

        when: "doBulk UPSERT records"
        ApiResults res = bulkImporter.doBulkSlice(upsertList, syncArgs)

        then:
        res.ok
        res.success.size() == 20

        and: "spot check ok result"
        OkResult res1 = res.success[1]
        res1 instanceof OkResult
        res1.status == HttpStatus.OK //should be 200

        and: "verify data"
        assert KitchenSink.count() == 20
        KitchenSink.list().each {
            if(it.id > 900){
                assert it.name == "inserted${it.id}"
            } else {
                assert it.name == "updated${it.id}"
            }
        }
    }
}
