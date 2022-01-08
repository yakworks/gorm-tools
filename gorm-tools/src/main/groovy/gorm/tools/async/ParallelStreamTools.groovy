/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import gorm.tools.support.ConfigAware

/**
 * Java 8 parallel streams implementation of the ParallelTools trait
 * to be used for colating/slicing a list into "batches" to then asynchronously process with Transactions
 * insert or update for maximum performance.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ParallelStreamTools implements ParallelTools, ConfigAware {

    ForkJoinPool forkJoinPool

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        // if (poolSize == 0) poolSize = 4 // Runtime.getRuntime().availableProcessors()
        forkJoinPool = new ForkJoinPool(asyncService.poolSize)
    }

    @Override
    public <T> Collection<T> each(AsyncConfig args, Collection<T> collection, Closure closure){
        boolean parEnabled = args.enabled != null ? args.enabled : asyncService.getAsyncEnabled()
        println("ParallelStreamTools each asyncEnabled $parEnabled")

        Consumer<T> wrappedConsumer = asyncService.wrapConsumer(args, closure as Consumer<T>)

        if (parEnabled) {
            int psize = args.poolSize ?: asyncService.getPoolSize()
            forkJoinPool.submit {
                collection.parallelStream().forEach(wrappedConsumer)
            }.join() //join makes it wait

        } else {
            collection.forEach(wrappedConsumer)
        }

        return collection
    }

}
