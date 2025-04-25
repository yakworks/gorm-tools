package yakworks.hazelcast

import com.hazelcast.collection.IQueue
import com.hazelcast.core.HazelcastInstance
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.springframework.beans.factory.annotation.Qualifier
import spock.lang.Specification
import yakworks.rally.api.HazelQueueListener
import yakworks.rally.orgs.model.Org

import javax.inject.Inject

@Integration
@Rollback
class HazelCastQueueSpec extends Specification {

    @Inject
    HazelQueueListener hazelQueueListener

    @Inject
    @Qualifier("hazelQueue")
    IQueue<Long> hazelQueue

    @Inject
    HazelcastInstance hazelcastInstance

    void "sanity check"() {
        expect:
        hazelQueueListener
        hazelQueue != null //its a queue, and needs explicit null check
        hazelcastInstance
    }

    void "put in queue"() {
        when:
        hazelQueue.put(11L)
        Thread.sleep(1000 * 3)

        then:
        noExceptionThrown()

        when:
        Org org = Org.get(11)

        then:
        org.comments == "updated-from-hazel-listener"

        and: "item should have been removed by listener"
        hazelQueue.size() == 0
    }

}
