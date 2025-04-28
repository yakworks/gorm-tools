/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.hazel

import java.util.concurrent.ThreadPoolExecutor

import groovy.transform.CompileStatic

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.task.TaskExecutorBuilder
import org.springframework.boot.task.TaskSchedulerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.CustomizableThreadFactory
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.TaskManagementConfigUtils
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor

import com.hazelcast.collection.IQueue
import com.hazelcast.core.HazelcastInstance

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Configuration @Lazy(false)
@CompileStatic
class HazelBeansConfig {

    public static String QUE_NAME = "demoQueue"

    @Bean
    public IQueue<Long> demoQueue(HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getQueue(QUE_NAME);
    }


}
