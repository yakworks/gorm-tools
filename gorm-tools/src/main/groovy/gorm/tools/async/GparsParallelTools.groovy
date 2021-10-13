/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovyx.gpars.GParsPoolUtil
import groovyx.gpars.util.PoolUtils

import gorm.tools.support.ConfigAware

import static groovyx.gpars.GParsPool.withPool

/**
 * a Gpars implementation of the ParallelTools trait
 * to be used for colating/slicing a list into "batches" to then asynchronously process with Transactions
 * insert or update for maximum performance.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */

@CompileStatic
class GparsParallelTools implements ParallelTools, ConfigAware {

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        if (poolSize == 0) poolSize = PoolUtils.retrieveDefaultPoolSize()
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        Integer batchSize = config.getProperty('hibernate.jdbc.batch_size', Integer)
        sliceSize = batchSize ?: sliceSize
    }


    @Override
    public <T> Collection<T> each(ParallelConfig args, Collection<T> collection, Closure closure){
        boolean gparsEnabled = args.enabled != null ? args.enabled : getAsyncEnabled()

        Closure wrappedClosure = wrapSessionOrTransaction(args, closure)

        if (gparsEnabled) {
            int psize = args.poolSize ?: getPoolSize()
            withPool(psize) {
                GParsPoolUtil.eachParallel(collection, wrappedClosure)
            }
        } else {
            collection.each(wrappedClosure)

        }

        return collection
    }


}
