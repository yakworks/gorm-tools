/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.hazel

import java.util.concurrent.BlockingQueue
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.task.TaskExecutor

@Slf4j
@CompileStatic
class DemoConsumer {

    private final BlockingQueue<Long> queue;
    private final DemoJobService demoJobService;

    public static final boolean CONSUMER_ENABLED = true

    @Autowired TaskExecutor taskExecutor;

    DemoConsumer(@Qualifier("demoJobQueue") BlockingQueue<Long> queue, DemoJobService demoJobService) {
        log.info("🔥🔥🔥🔥               DemoConsumer new")
        this.queue = queue;
        this.demoJobService = demoJobService;
    }

    @PostConstruct
    void init() {
        startConsuming();
    }

    public void startConsuming() {
        //run Async, the @Async annotation is goofy in ApplicationListeners
        taskExecutor.execute(() -> {
            consumeMessages()
        });
    }

    private void consumeMessages() {
        while (CONSUMER_ENABLED) {
            try {
                Long jobId = queue.take(); // Will block until an event is available or interrupted
                // Process the event
                log.info("🔥🔥🔥🔥 EventConsumer::: Processing jobId {} ", jobId);
                demoJobService.runJob(jobId);
            } catch (InterruptedException e) {
                // Handle InterruptedException as per app requirements
                log.error("Encountered thread interruption: ", e);
                Thread.currentThread().interrupt();
            }
        }
    }

}
