/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.hazel

import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicLong

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import com.hazelcast.collection.IQueue
import com.hazelcast.core.HazelcastInstance

/**
 * Job to scan for and process the ACH Ack files
 * see https://github.com/kmandalas/spring-hazelcast-competing-consumers/tree/main/consumer
 * examples taken from there
 */
@Slf4j
@CompileStatic
//@Component
class DemoSpringJobs {

    @Autowired HazelcastInstance hazelcastInstance
    @Autowired DemoJobService demoJobService
    @Autowired BlockingQueue<Long> demoJobQueue

    AtomicLong nextId = new AtomicLong(1)

    // @Scheduled(fixedDelay = 2000L)
    // public void demoJob() {
    //     log.info("🤡  Running Job\n")
    //     Thread.sleep(5000);
    //     log.info("✅  Finished Job\n")
    // }

    //@Scheduled(fixedDelay = 5000L)
    public void consumerJob() {
        log.info("🤡  Consumer Running \n")
        //Thread.sleep(5000);
        //var bque = getQueue()
        var bque = demoJobQueue
        while(true) {
            // take() - Retrieves and removes the head of this queue, waits until one becomes available.
            // poll() - Retrieves and removes the head of this queue, returns null if this queue is empty. Can add a timeout for how long to wait
            Long qid = bque.poll()
            // process event
            if (qid != null) {
                log.info("Found one consumerJob poller::: Processing {} ", qid);
                demoJobService.runJob(qid);
            } else {
                log.info("null poll, breaking")
                break;
            }
        }
        log.info("✅  Consumer Finished\n")
    }

    /**
     * Sticks stuff in the queue every 2 seconds
     */
    @Scheduled(fixedDelay = 10000L)
    public void producerJob() {
        log.info("🌶  OFFER some items on queue\n")
        BlockingQueue<Long> bque = getQueue()
        // offer returns true or false, can also pass in timeout
        // add throws exception if not space
        // put will block until it can be added
        (1..3).each {
            Long jobId = nextId.getAndIncrement()
            bque.offer(jobId)
            log.info("✅  OFFER Finished adding ${jobId} to queue\n")
        }
    }

    IQueue<Long> getQueue(){
        hazelcastInstance.getQueue(HazelBeansConfig.QUE_NAME) as IQueue<Long>
    }
}
