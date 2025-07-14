/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.jobqueue

import java.util.concurrent.BlockingQueue

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled

import gorm.tools.job.events.SyncJobQueueEvent
import gorm.tools.repository.model.DataOp
import yakworks.gorm.api.bulk.BulkExportJobArgs
import yakworks.gorm.api.bulk.BulkExportService
import yakworks.gorm.api.bulk.BulkImportJobArgs
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.model.KitchenSink

/**
 * POC Test Jobs that put things on the SyncJob queue
 */
@Slf4j
@Configuration @Lazy(false)
@Profile("(hazel | ignite) & server")
@CompileStatic
class TestProducerConfiguration {

    @Autowired BlockingQueue<SyncJob> syncJobQueue

    /**
     * Listen for SyncJobQueueEvent and offer/put it on the queue
     * @param event
     */
    @EventListener
    void syncJobQueueEventListener(SyncJobQueueEvent event) {
        syncJobQueue.offer((SyncJob)event.syncJob)
        log.info("⏱️📤   LISTENER Finished adding ${event.syncJob} to queue")
    }

    /**
     * Sticks stuff in the queue every 2 seconds
     */
    @Scheduled(fixedDelay = 10_000L, initialDelay = 5000L)
    public void producerJob() {
        log.info("  OFFER some jobs on queue")
        //BlockingQueue<SyncJob> syncJobQueue = getJobQueue()
        // offer returns true or false, can also pass in timeout
        // add throws exception if not space
        // put will block until it can be added
        //IMPORT
        (1..2).each {
            var job = submitImportJob()
            //see the Listener in the consumer config
            //syncJobQueue.offer(job)
            log.info("⏱️🗂️ Finished submitImportJob")
        }
        //EXPORT
        (1..2).each {
            var jobEx = submitExportJob()
            //see the Listener in the consumer config
            //syncJobQueue.offer(jobEx)
            log.info("⏱️📤   Finished submitExportJob")
        }
        //sleep(10000)
        log.info(" END OFFER")
    }

    //can inject it or do this to get it
    // IgniteQueue<SyncJob> getJobQueue(){
    //     var colConfig = new CollectionConfiguration()
    //     //colConfig.setCacheMode(CacheMode.REPLICATED)
    //     colConfig.backups = 1
    //
    //     //will get existing que or create one if it doenst exist.
    //     return igniteInstance.queue(QUE_NAME, // Queue name.
    //         0, // Queue capacity. 0 for an unbounded queue.
    //         colConfig //default config
    //     )
    //     //igniteInstance.queue(QUE_NAME, 0, null) as IgniteQueue<SyncJob>
    // }

    SyncJob submitImportJob(){
        var bulkImportService = BulkImportService.lookup(KitchenSink)

        //pick random from either 999 or 10k so it checks both payloadId attachment and when under 1k stores in column
        int dataSize = new Random().nextBoolean() ? 1000 : 10_000;
        List dataList = KitchenSink.generateDataList(dataSize)

        // Map params = [
        //     parallel: false, async:false,
        //     source: "test", sourceId: "test-job", includes: ["id", "name", "ext.name"]
        // ]
        // SyncJob jobEnt = (SyncJob)bulkImportService.queueImportJob(DataOp.add, params, "test-job", dataList)
        def bimpParams = new BulkImportJobArgs(
            op: DataOp.add,
            parallel: false, async: false,
            sourceId: 'test-job', includes: ["id", "name", "ext.name"]
        )
        var jobEnt = bulkImportService.queueJob(bimpParams, dataList)

        return jobEnt as SyncJob
    }

    SyncJob submitExportJob(){
        var bulkExportService = BulkExportService.lookup(KitchenSink)

        BulkExportJobArgs jobParams = new BulkExportJobArgs(
            q: '{"id":{"$gte":1}}',
            sourceId: "test-job", includes: ["id", "name", "ext.name"]
        )
        SyncJob jobEnt = (SyncJob)bulkExportService.queueJob(jobParams)
        return jobEnt
    }

}
