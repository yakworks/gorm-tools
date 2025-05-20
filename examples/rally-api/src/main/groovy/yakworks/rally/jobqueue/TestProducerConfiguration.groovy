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
import org.springframework.scheduling.annotation.Scheduled

import gorm.tools.repository.model.DataOp
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.rally.job.SyncJob
import yakworks.testing.gorm.model.KitchenSink

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Slf4j
@Configuration @Lazy(false)
@Profile("server")
@CompileStatic
class TestProducerConfiguration {

    @Autowired BlockingQueue<SyncJob> syncJobQueue

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
        (1..10).each {
            var job = createJob()
            syncJobQueue.offer(job)
            log.info("🌶  OFFER Finished adding ${job.id} to queue\n")
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

    SyncJob createJob(){
        // return new SyncJob(
        //     sourceType: SourceType.ERP, sourceId: 'ar/org',
        //     jobType: 'bulk.import',
        //     state: SyncJobState.Queued
        // ).persist(flush:true)

        var bulkImportService = BulkImportService.lookup(KitchenSink)

        List dataList = KitchenSink.generateDataList(1000)

        Map params = [
            parallel: false, async:false,
            source: "test", sourceId: "test-job", includes: ["id", "name", "ext.name"]
        ]
        SyncJob jobEnt = (SyncJob)bulkImportService.queueImportJob(DataOp.add, params, "test-job", dataList)
        return jobEnt
    }

}
