/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.jobqueue

import java.util.concurrent.BlockingQueue

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.ignite.Ignite
import org.apache.ignite.IgniteQueue
import org.apache.ignite.configuration.CollectionConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled

import gorm.tools.job.SyncJobState
import gorm.tools.model.SourceType
import yakworks.rally.job.SyncJob

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Slf4j
@Configuration @Lazy(false)
@CompileStatic
class IgniteQueueBeansConfig {

    @Autowired Ignite igniteInstance

    public static final String QUE_NAME = "syncJobQueue"

   // @Autowired HazelcastInstance hazelcastInstance

    @Configuration
    @Profile('!test')
    @Lazy(false) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class DemoBeans {

        @Autowired Ignite igniteInstance

        @Bean
        public BlockingQueue<SyncJob> syncJobQueue(Ignite igniteInstance) {

            return igniteInstance.queue(QUE_NAME, // Queue name.
                0, // Queue capacity. 0 for an unbounded queue.
                new CollectionConfiguration() //default config
            )
        }

        // @Bean
        // public DemoConsumer syncJobConsumer(BlockingQueue<Long> demoJobQueue, DemoJobService demoJobService) {
        //     new DemoConsumer(demoJobQueue, demoJobService);
        // }
        //
        // @Bean
        // public DemoSpringJobs demoSpringJobs() {
        //     new DemoSpringJobs()
        // }
    }

    @Configuration
    @Profile('!test')
    @Lazy(false) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class IgniteSyncJobs {

        @Autowired Ignite igniteInstance
        @Autowired BlockingQueue<SyncJob> syncJobQueue

        /**
         * Sticks stuff in the queue every 2 seconds
         */
        @Scheduled(fixedDelay = 10_000L, initialDelay = 5000L)
        public void producerJob() {
            log.info("  OFFER some jobs on queue")
            BlockingQueue<SyncJob> bque = getJobQueue()
            // offer returns true or false, can also pass in timeout
            // add throws exception if not space
            // put will block until it can be added
            (1..3).each {
                var job = createJob()
                bque.offer(job)
                log.info("🌶  OFFER Finished adding ${job.id} to queue\n")
            }
            //sleep(10000)
            log.info(" END OFFER")
        }

        //@Scheduled(fixedDelay = 2000L)
        public void consumerJob() {
            log.info("🤡  Consumer Running ")
            //Thread.sleep(5000);
            //var bque = getQueue()
            var bque = syncJobQueue
            //with this logic will spin through and grab as many as on the pool while in here and run them
            while(true) {
                // take() - Retrieves and removes the head of this queue, waits until one becomes available.
                // poll() - Retrieves and removes the head of this queue, returns null if this queue is empty. Can add a timeout for how long to wait
                var job = bque.poll()
                // process event
                if (job != null) {
                    log.info("Found one consumerJob poller::: Processing {} ", job);
                    //demoJobService.runJob(qid);
                } else {
                    log.info("null poll, breaking")
                    break;
                }
            }
            log.info("✅  Consumer Finished\n")
        }

        //Spring will only run one of these at a time no matter how big the threadpool is.
        // so if this takes 5 seconds, the next one will run 2 seconds after its finished.
        @Scheduled(fixedDelay = 2000L)
        public void consumerJob2() {
            log.info("  Consumer2 Running ")
            var job = syncJobQueue.poll()
            // process event
            if (job != null) {
                //sleep(5000)
                log.info("🤡  Consumer2 Found one consumerJob poller::: Processing {} ", job);
                //demoJobService.runJob(qid);
            }
            //sleep(5000)
            //throw new RuntimeException("ex test")
            log.info("  Consumer2 Finished\n")
        }

        //can inject it or do this
        IgniteQueue<SyncJob> getJobQueue(){
            igniteInstance.queue(QUE_NAME, 0, null) as IgniteQueue<SyncJob>
        }

        SyncJob createJob(){
            return new SyncJob(
                sourceType: SourceType.ERP, sourceId: 'ar/org',
                jobType: 'bulk.import',
                state: SyncJobState.Queued
            ).persist(flush:true)
        }
    }




}
