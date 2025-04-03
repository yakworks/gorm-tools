package yakworks.rally.api

import com.hazelcast.collection.IQueue
import com.hazelcast.collection.ItemEvent
import com.hazelcast.collection.ItemListener
import com.hazelcast.core.HazelcastInstance
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Qualifier
import yakworks.rally.orgs.model.Org

import javax.inject.Inject

@Slf4j
class HazelQueueListener implements ItemListener<Long>{

    @Inject HazelcastInstance hazelcastInstance

    @Inject
    @Qualifier("hazelQueue")
    IQueue<Long> hazelQueue

    @Override
    void itemAdded(ItemEvent<Long> event) {
        updateOrg(event.item)
    }

    @Override
    void itemRemoved(ItemEvent<Long> item) {
        log.info("Queue item removed $item")
    }

    @Transactional
    void updateOrg(Long id) {
        Org.update([id:id, comments: "updated-from-hazel-listener"])
        hazelQueue.remove(id)
    }
}
