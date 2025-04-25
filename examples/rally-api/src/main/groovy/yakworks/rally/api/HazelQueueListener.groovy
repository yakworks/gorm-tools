/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import javax.inject.Inject

import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Qualifier

import com.hazelcast.collection.IQueue
import com.hazelcast.collection.ItemEvent
import com.hazelcast.collection.ItemListener
import com.hazelcast.core.HazelcastInstance
import grails.gorm.transactions.Transactional
import yakworks.rally.orgs.model.Org

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
