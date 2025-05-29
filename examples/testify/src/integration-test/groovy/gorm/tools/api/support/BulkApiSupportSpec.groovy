package gorm.tools.api.support

import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.api.bulk.BulkImportJobParams
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.integration.DomainIntTest
import yakworks.testing.gorm.model.KitchenSink

@Integration
@Rollback
class BulkApiSupportSpec extends Specification implements DomainIntTest {

    void "sanity check"() {
        when:
        BulkImportService bs = BulkImportService.lookup(KitchenSink)

        then:
        noExceptionThrown()
        bs
        bs.syncJobService
        bs.entityClass == KitchenSink
    }

    void "test queueImportJob"() {
        when:
        BulkImportService bs = BulkImportService.lookup(Org)
        def bimpParams = new BulkImportJobParams( op: DataOp.add,
            sourceId: 'test-job',
            q: "{\"typeId\": ${OrgType.Customer.id}}",
            attachmentId:1L
        )
        SyncJob job = bs.queueImportJob(bimpParams, [[num:"T1", name:"T1"]])

        //SyncJob job = bs.queueImportJob(DataOp.add, [q:[typeId: OrgType.Customer.id], attachmentId:1L], "test-job", [[num:"T1", name:"T1"]])
        flushAndClear()
        assert job.id
        job = SyncJob.get(job.id)

        then:
        noExceptionThrown()
        job
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'
        job.params
        job.params.q == '{"typeId": 1}'
        job.payloadId
        job.payloadId == 1L
    }

}
