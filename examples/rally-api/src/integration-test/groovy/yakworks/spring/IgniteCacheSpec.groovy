package yakworks.spring

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteCache
import org.apache.ignite.cache.spring.IgniteCacheManager
import org.springframework.beans.factory.annotation.Autowired

import com.hazelcast.core.HazelcastInstance
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.rally.job.SyncJob
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.integration.DomainIntTest

// Use as a simple to test when trying to see why application context has problem on init
@Integration
@Ignore //XXX @SUD can we run a test pass with profile=ignite so this can run and pass?
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
        SyncJobArgs syncJobArgs = new SyncJobArgs(
            sourceId: '123', source: 'some source', payload: [1,2,3], jobType: 'bulk.import'
        )
        syncJobArgs.entityClass = Org
        SyncJobEntity job = syncJobService.queueJob(syncJobArgs)
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
