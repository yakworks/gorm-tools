/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import groovy.transform.CompileStatic
import groovyx.gpars.GParsPoolUtil

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
@Deprecated
@CompileStatic
class GparsParallelTools implements ParallelTools, ConfigAware {

    @Override
    public <T> Collection<T> each(AsyncConfig args, Collection<T> collection, Closure closure){
        boolean gparsEnabled = args.enabled != null ? args.enabled : asyncService.getAsyncEnabled()

        Closure wrappedClosure = asyncService.wrapClosure(args, closure)

        if (gparsEnabled) {
            int psize = args.poolSize ?: asyncService.getPoolSize()
            withPool(psize) {
                GParsPoolUtil.eachParallel(collection, wrappedClosure)
            }
        } else {
            collection.each(wrappedClosure)

        }

        return collection
    }


}
