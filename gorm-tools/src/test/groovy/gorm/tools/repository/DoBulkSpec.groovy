package gorm.tools.repository

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.repository.model.DataOp
import spock.lang.Specification
import testing.TestSyncJob
import yakworks.api.ApiResults
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.KitchenSinkRepo
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.unit.GormHibernateTest

class DoBulkSpec extends Specification implements GormHibernateTest {
    static entityClasses = [KitchenSink, SinkExt, TestSyncJob]
    // static springBeans = [syncJobService: TestSyncJobService]

    @Autowired KitchenSinkRepo kitchenSinkRepo

    SyncJobArgs setupSyncJobArgs(DataOp op = DataOp.add){
        return new SyncJobArgs(asyncEnabled: false, op: op, source: "test", sourceId: "test",
            includes: ["id", "name", "ext.name"])
    }

    void "success doBulk add"() {
        given:
        List list = KitchenSink.generateDataList(100)
        def syncArgs = setupSyncJobArgs()

        when: "doBulk insert records"
        def jobContext = new SyncJobContext(args: syncArgs, payload: list )
        ApiResults res = kitchenSinkRepo.doBulk(list, jobContext)

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
        payload1['name'] == "Blue Cheese"

        //check more?
        // res.list[0].ext.name == "SinkExt1"
        //sanity check
        // res.list[9].name == "Oranges"

        and: "Verify database records"
        def bcks = KitchenSink.findByName("Blue Cheese")
        bcks
        bcks.ext.name == "SinkExt1"

        KitchenSink.count() == 100
        KitchenSink.findByName("Oranges")
    }

    // void "success doBulk update"() {
    //     //TODO, use BulkableRepo as example starting point
    // }
    //
    // void "success doBulk errors"() {
    //     //TODO, use BulkableRepo as example starting point
    // }
}
