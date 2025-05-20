/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.jobqueue

import java.util.concurrent.BlockingQueue

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.ignite.Ignite
import org.apache.ignite.configuration.CollectionConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled

import com.hazelcast.core.HazelcastInstance
import yakworks.rally.job.SyncJob

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Slf4j
@Configuration
@CompileStatic
class SyncJobConsumerConfiguration {

    //@Autowired Ignite igniteInstance

    public static final String QUE_NAME = "syncJobQueue"

   // @Autowired HazelcastInstance hazelcastInstance

    @Configuration
    @Profile('ignite & !test')
    @Lazy(true) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class IgniteQueBeans {

        @Bean(destroyMethod="") //empty detroy method is important or spring will clear it on other clusters if it shuts down
        public BlockingQueue<SyncJob> syncJobQueue(Ignite igniteInstance) {
            var colConfig = new CollectionConfiguration()
            //colConfig.setCacheMode(CacheMode.REPLICATED)
            colConfig.backups = 1

            return igniteInstance.queue(QUE_NAME, // Queue name.
                0, // Queue capacity. 0 for an unbounded queue.
                colConfig //default config
            )
        }

    }

    @Configuration
    @Profile('hazel & !test')
    @Lazy(true) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class HazelQueBeans {

        @Bean(destroyMethod="") //empty detroy method is important or spring will clear it on other clusters if it shuts down
        public BlockingQueue<SyncJob> syncJobQueue(HazelcastInstance hazelcastInstance) {
            hazelcastInstance.getQueue(QUE_NAME)
        }

    }

    @Configuration
    @Profile(['!test'])
    @Lazy(false) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class IgniteSyncConsumerJobs {

        @Autowired BlockingQueue<SyncJob> syncJobQueue
        @Autowired QueuedJobRunner queuedJobRunner

        //Spring will only run one of these at a time no matter how big the threadpool is.
        // so if this takes 5 seconds, the next one will run 2 seconds after its finished.
        @Scheduled(fixedDelay = 2000L)
        public void consumerJob2() {
            log.info("  Consumer2 Running ")
            var job = syncJobQueue.poll() //getJobQueue().poll() //syncJobQueue.poll()
            // process event
            if (job != null) {
                //log.info("🤡  Consumer2 Found one consumerJob poller::: Processing {} ", job);
                queuedJobRunner.runJob(job)
            }
            //sleep(5000)
            //throw new RuntimeException("ex test")
            log.info("  Consumer2 Finished\n")
        }

    }


}
