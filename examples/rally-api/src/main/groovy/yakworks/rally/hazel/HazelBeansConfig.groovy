/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.hazel

import java.util.concurrent.BlockingQueue

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile

import com.hazelcast.core.HazelcastInstance

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Configuration @Lazy(false)
@CompileStatic
class HazelBeansConfig {

    public static final String QUE_NAME = "demoJobQueue"

   // @Autowired HazelcastInstance hazelcastInstance

    @Configuration
    @Profile('!test')
    @Lazy(false) //lazy false so that consumer bean gets registered
    //@ConditionalOnProperty(value="app.mail.mailgun.enabled", havingValue = "true")
    static class DemoBeans {
        @Bean
        public BlockingQueue<Long> demoJobQueue(HazelcastInstance hazelcastInstance) {
            return hazelcastInstance.getQueue(QUE_NAME);
        }

        @Bean
        public DemoConsumer demoConsumer(BlockingQueue<Long> demoJobQueue, DemoJobService demoJobService) {
            new DemoConsumer(demoJobQueue, demoJobService);
        }

        @Bean
        public DemoSpringJobs demoSpringJobs() {
            new DemoSpringJobs()
        }
    }




}
