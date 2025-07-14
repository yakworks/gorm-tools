package yakworks.spring

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.spring.IgniteCacheManager
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.gorm.api.bulk.BulkImportJobArgs
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Ignore
//@Rollback
class IgniteCacheSpec extends Specification implements DomainIntTest {

    @Autowired IgniteCacheManager igniteCacheManager
    @Autowired Ignite igniteInstance
    @Autowired SyncJobService syncJobService

    void "sanity check"() {
        when:
        assert igniteCacheManager
        igniteCacheManager.getCache("fooCache").put("foo", "bar")

        then:
        igniteCacheManager.getCache("fooCache").get("foo").get() == 'bar'
    }

    void "try with gorm object"() {
        when:
        BulkImportJobArgs bulkImportJobArgs = BulkImportJobArgs.fromParams(
            sourceId: '123', source: 'some source', payload: [1,2,3], jobType: BulkImportJobArgs.JOB_TYPE
        )
        def payload = [1,2,3]
        bulkImportJobArgs.entityClass = Org
        var bulkImportService = BulkImportService.lookup(Org)
        SyncJobEntity job = bulkImportService.queueJob(bulkImportJobArgs, [1,2,3])
        var cache = igniteCacheManager.getCache("jobCache")

        cache.put(job.id, job)

        SyncJobEntity cachedJob = cache.get(job.id).get()

        then:
        cachedJob instanceof SyncJob
        cachedJob.id == job.id
        cachedJob.payloadToString() == '[1,2,3]'

        when:
        //get straight from ignite now
        IgniteCache<Long, SyncJob> igJobCache = igniteInstance.cache("jobCache");
        SyncJobEntity cachedJob2 = igJobCache.get(job.id)

        then:
        cachedJob2 instanceof SyncJob
        cachedJob2.id == job.id
        cachedJob2.payloadToString() == '[1,2,3]'

    }

}
