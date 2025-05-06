package gorm.tools.api.support

import gorm.tools.job.SyncJobState
import gorm.tools.repository.model.DataOp
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.gorm.api.support.BulkApiSupport
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
        BulkApiSupport bs = BulkApiSupport.of(KitchenSink)

        then:
        noExceptionThrown()
        bs
        bs.syncJobService
        bs.entityClass == KitchenSink
    }

    void "test submitJob"() {
        setup:
        BulkApiSupport bs = BulkApiSupport.of(Org)

        when:
        SyncJob job = bs.submitJob(DataOp.add, [q:[typeId: OrgType.Customer.id], attachmentId:1L], "test-job", [[num:"T1", name:"T1"]])
        flushAndClear()
        assert job.id
        job = SyncJob.get(job.id)

        then:
        noExceptionThrown()
        job
        job.state == SyncJobState.Queued
        job.sourceId == 'test-job'
        job.params
        job.params.q == [typeId: OrgType.Customer.id]
        job.payloadId
        job.payloadId == 1L
    }
}
