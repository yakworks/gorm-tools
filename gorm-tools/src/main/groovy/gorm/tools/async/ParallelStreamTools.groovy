/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

/**
 * Java 8 parallel streams implementation of the ParallelTools trait
 * to be used for colating/slicing a list into "batches" to then asynchronously process with Transactions
 * insert or update for maximum performance.
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class ParallelStreamTools implements ParallelTools {

    ForkJoinPool forkJoinPool

    /** setup defaults for poolSize and batchSize from asyncService*/
    @PostConstruct
    void init() {
        ClassLoaderThreadFactory factory = new ClassLoaderThreadFactory()
        // if (poolSize == 0) poolSize = 4 //  Math.min(32767, Runtime.getRuntime().availableProcessors()),
        forkJoinPool = new ForkJoinPool(getAsyncConfig().poolSize, factory, null, false)
        // forkJoinPool = new ForkJoinPool(getAsyncConfig().poolSize)
    }

    @Override
    public <T> Collection<T> each(AsyncArgs args, Collection<T> collection, Closure closure){
        boolean parEnabled = args.enabled != null ? args.enabled : asyncConfig.enabled
        // println("ParallelStreamTools each asyncEnabled $parEnabled")

        Consumer<T> wrappedConsumer = asyncService.wrapConsumer(args, closure as Consumer<T>)

        if (parEnabled) {
            int psize = args.poolSize ?: asyncConfig.poolSize
            forkJoinPool.submit {
                collection.parallelStream().forEach(wrappedConsumer)
            }.join() //join makes it wait

        } else {
            collection.forEach(wrappedConsumer)
        }

        return collection
    }

}
