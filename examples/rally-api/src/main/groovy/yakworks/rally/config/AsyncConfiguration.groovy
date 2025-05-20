/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.config

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

/**
 * Spring config for Async related beans.
 * NOTE: @Lazy(false) to make sure Jobs are NOT Lazy, they need to be registered at init to get scheduled.
 */
@Configuration @Lazy(false)
@EnableAsync
@EnableScheduling
@CompileStatic
class AsyncConfiguration implements AsyncConfigurer {

    /**
     * Copied in from TaskExecutionAutoConfiguration
     * The TaskExecutorBuilder is setup from the TaskExecutionProperties
     * see the config/spring.yml for settings
     * @see org.springframework.boot.autoconfigure.task.TaskExecutionProperties
     */
    @Bean
    ThreadPoolTaskExecutor applicationTaskExecutor(TaskExecutorBuilder builder) {
        ThreadPoolTaskExecutor executor = builder.build();
        // if que is at capacity, then caller thread will run it, will slow down caller thread
        // but wont error or crush the server if we get 1000's of asyncs for some rogue process
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.threadFactory = getClassLoaderCustomizableThreadFactory(executor.getThreadNamePrefix())
        return executor;
    }

    /**
     * The DEFAULT taskExecutor wraps the thread with who ever is logged in
     * see https://www.baeldung.com/spring-security-async-principal-propagation
     * DelegatingSecurityContextAsyncTaskExecutor makes sure the spring security is passed down from parent thread
     */
    @Bean
    DelegatingSecurityContextAsyncTaskExecutor taskExecutor(ThreadPoolTaskExecutor applicationTaskExecutor) {
        return new DelegatingSecurityContextAsyncTaskExecutor(applicationTaskExecutor);
    }

    /**
     * FIXME look at DelegatingSecurityContextTaskScheduler so we can secService.loginAsSystemUser()
     * @see org.springframework.boot.autoconfigure.task.TaskSchedulingProperties
     */
    @Bean
    @ConditionalOnBean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    //@ConditionalOnMissingBean({ SchedulingConfigurer.class, TaskScheduler.class, ScheduledExecutorService.class })
    ThreadPoolTaskScheduler taskScheduler(TaskSchedulerBuilder builder) {
        ThreadPoolTaskScheduler sched = builder.build();
        //default is one, which means on 1 @Scheduled can be running at a time across the app.
        sched.setPoolSize(4)
        sched.threadFactory = getClassLoaderCustomizableThreadFactory(sched.getThreadNamePrefix())
        return sched
    }

    //see https://stackoverflow.com/a/59444016/6500859
    //FIXME this might be angry monkey thing. Should remove and test.
    // goal here is to use the main classloader with java11 so we dont get the ClassNotFoundException
    CustomizableThreadFactory getClassLoaderCustomizableThreadFactory(String threadNamePrefix){
        ClassLoader invokingThreadCL = Thread.currentThread().getContextClassLoader()
        return new CustomizableThreadFactory(threadNamePrefix){
            @Override
            public Thread newThread(Runnable runnable) {
                Thread t = createThread(runnable)
                t.setContextClassLoader(invokingThreadCL)
                return t
            }
        }
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        //if uncaught exeptions this will just log it out to errors
        return new SimpleAsyncUncaughtExceptionHandler();
    }


}

//Links
//https://reflectoring.io/spring-scheduler/
// why use @Async with @Scheduled
// https://medium.com/@ali.gelenler/deep-dive-into-spring-schedulers-and-async-methods-27b6586a5a17

//EXAMPLE, Offload expensive parts of your tasks to a different executor so it doesnt hold up the scheduler threads
/*
public class ScheduledAsyncTask implements DisposableBean {

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Scheduled(fixedRate = 10000)
    public void scheduleFixedRateTaskAsync() throws InterruptedException {
        executorService.submit(() -> {
            // Expensive calculations ...
        });
    }

    @Override
    public void destroy() {
        executorService.shutdown();
    }
}
*/
