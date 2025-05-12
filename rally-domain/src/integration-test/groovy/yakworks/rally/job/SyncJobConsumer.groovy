package yakworks.rally.job

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.task.TaskExecutor

import javax.annotation.PostConstruct
import java.util.concurrent.BlockingQueue

/**
 * Consumer, that consumes jobs submitted to hazelcast queue and runs the jobs.
 *
 * FIXME @SUD WIP, finish.
 */
@Slf4j
@CompileStatic
class SyncJobConsumer {
    public static final boolean CONSUMER_ENABLED = true

    @Autowired TaskExecutor taskExecutor
    private final BlockingQueue<Long> queue

    SyncJobConsumer(BlockingQueue queue) {
        this.queue = queue
    }

    @PostConstruct
    void init() {
        startConsuming()
    }

    void startConsuming() {
        taskExecutor.execute(() -> {
            consumeMessages()
        })
    }

    private void consumeMessages() {
        while (CONSUMER_ENABLED) {
            try {
                // Will block until an event is available or interrupted
                Long jobId = queue.take()
                // Process the event
                log.info("🔥🔥🔥🔥 EventConsumer::: Processing jobId {} ", jobId)
                //BulkApiSupport bulkApiSupport = BulkApiSupport.of(entityClass)
                //bulkApiSupport.startJob(jobId)
            } catch (InterruptedException e) {
                // Handle InterruptedException as per app requirements
                log.error("Encountered thread interruption: ", e)
                Thread.currentThread().interrupt()
            }
        }
    }
}
