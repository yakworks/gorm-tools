/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import java.util.concurrent.ForkJoinPool
import java.util.function.Consumer
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.Datastore
import org.springframework.transaction.TransactionStatus

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
        if (poolSize == 0) poolSize = 4 // Runtime.getRuntime().availableProcessors()
        forkJoinPool = new ForkJoinPool(poolSize)
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        Integer batchSize = config.getProperty('hibernate.jdbc.batch_size', Integer)
        sliceSize = batchSize ?: sliceSize
    }

    @Override
    public <T> Collection<T> each(ParallelConfig args, Collection<T> collection, Closure closure){
        boolean parEnabled = args.enabled != null ? args.enabled : getAsyncEnabled()

        Consumer<T> wrappedConsumer = consumerSessionOrTransaction(args, closure) as Consumer<T>

        if (parEnabled) {
            int psize = args.poolSize ?: getPoolSize()
            forkJoinPool.submit {
                collection.parallelStream().forEach(wrappedConsumer)
            }.join() //join makes it wait

        } else {
            collection.forEach(wrappedConsumer)
        }

        return collection
    }

    public <T> Consumer<T> consumerSessionOrTransaction(ParallelConfig asyncArgs, Closure<T> closure){
        Consumer<T> wrappedConsumer

        if(asyncArgs.transactional){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerTrx(asyncArgs.datastore, closure)
        } else if(asyncArgs.session){
            verifyDatastore(asyncArgs)
            wrappedConsumer = wrapConsumerSession(asyncArgs.datastore, closure)
        } else {
            // no wrap so just do closure
            wrappedConsumer = closure as Consumer<T>
        }
        wrappedConsumer
    }

    public <T> Consumer<T> wrapConsumerTrx(Datastore ds, Closure<T> c) {
        return new Consumer<T>() {
            @Override
            void accept(T item) {
                getTrxService().withTrx(ds) { TransactionStatus status ->
                    c.call(item)
                }
            }
        }
    }

    @SuppressWarnings(["EmptyCatchBlock"])
    public <T> Consumer<T> wrapConsumerSession(Datastore ds, Closure<T> wrapped) {
        return new Consumer<T>() {
            @Override
            void accept(T item) {
                persistenceInterceptor.init()
                try {
                    wrapped.call(item)
                } finally {
                    try {
                        //only destroys if new one was created, otherwise does nothing
                        persistenceInterceptor.destroy()
                    } catch (Exception e) {
                        //ignore errors
                    }
                }
            }
        }
    }


}
