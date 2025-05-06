/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import java.util.concurrent.BlockingQueue

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Qualifier

import com.hazelcast.collection.ICollection
import com.hazelcast.collection.ItemEvent
import com.hazelcast.collection.ItemListener
import yakworks.rally.hazel.DemoJobService

@Slf4j
//@Component @Lazy(false)
@CompileStatic
class DemoQueueListener implements ItemListener<Long>{

    //@Inject HazelcastInstance hazelcastInstance
    //@Autowired
    DemoJobService demoJobService
    //@Autowired
    BlockingQueue<Long> demoJobQueue

    // @Inject
    // @Qualifier("hazelQueue")
    // IQueue<Long> hazelQueue

    DemoQueueListener(@Qualifier("demoJobQueue") BlockingQueue<Long> demoJobQueue, DemoJobService demoJobService) {
        log.info("🔥🔥🔥🔥               DemoQueueListener new")
        this.demoJobQueue = demoJobQueue;
        this.demoJobService = demoJobService;
        (this.demoJobQueue as ICollection).addItemListener(this, true);
    }

    @Override
    void itemAdded(ItemEvent<Long> event) {
        log.info("🔥  Queue item added")
    }

    @Override
    void itemRemoved(ItemEvent<Long> item) {
        log.info("🔥  Queue item removed $item")
    }

}
