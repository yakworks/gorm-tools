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
@Component
class DemoJob {

    @Autowired HazelcastInstance hazelcastInstance
    AtomicLong nextId = new AtomicLong(1)

    // @Scheduled(fixedDelay = 2000L)
    // public void demoJob() {
    //     log.info("🤡  Running Job\n")
    //     Thread.sleep(5000);
    //     log.info("✅  Finished Job\n")
    // }

    @Scheduled(fixedDelay = 5000L)
    public void consumerJob() {
        log.info("🤡  Consumer Running \n")
        //Thread.sleep(5000);
        var bque = getQueue()
        while(true) {
            // take() - Retrieves and removes the head of this queue, waits until one becomes available.
            // poll() - Retrieves and removes the head of this queue, returns null if this queue is empty. Can add a timeout for how long to wait
            Long qid = bque.poll()
            // process event
            if (qid != null) {
                log.info("Found one consumerJob poller::: Processing {} ", qid);
                //demoService.doWork(event);
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
    @Scheduled(fixedDelay = 1000L)
    public void producerJob() {
        log.info("🌶  OFFER item on queue\n")
        BlockingQueue<Long> bque = getQueue()
        // offer returns true or false, can also pass in timeout
        // add throws exception if not space
        // put will block until it can be added
        bque.offer(nextId.getAndIncrement())
        log.info("✅  OFFER Finished adding item to queue\n")
    }

    IQueue<Long> getQueue(){
        hazelcastInstance.getQueue(HazelBeansConfig.QUE_NAME) as IQueue<Long>
    }
}
