/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SecondParam
import groovyx.gpars.GParsPoolUtil
import groovyx.gpars.util.PoolUtils

import org.springframework.beans.factory.annotation.Value

import static groovyx.gpars.GParsPool.withPool

/**
 * a Gpars implementation of the AsyncSupport trait
 * to be used for colating/slicing a list into "batches" to then asynchronously process with Transactions
 * insert or update for maximum performance.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class GparsAsyncSupport implements AsyncSupport {

    /** the pool size passed to GParsPool.withPool. if gpars.poolsize is not set then it uses PoolUtils.retrieveDefaultPoolSize()*/
    @Value('${gpars.poolsize:0}')
    int poolSize

    /** setup defaults for poolSize and batchSize if config isn't present. batchSize set to 100 if not config found*/
    @PostConstruct
    void init() {
        if (poolSize == 0) poolSize = PoolUtils.retrieveDefaultPoolSize()
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        if (batchSize == 0) batchSize = 100
    }

    @Override
    void parallel(Map args, Collection collection, Closure closure) {

        boolean gparsEnabled = args.asyncEnabled as Boolean ? args.asyncEnabled as Boolean : getAsyncEnabled()

        if (gparsEnabled) {
            eachParallel(collection, closure)
        } else {
            collection.each(wrapSession(args, closure))
        }

    }

    // @Override
    // void parallel(Map args, List<List> collection, Closure closure){
    //
    //     parallel(args, collection, closure)
    //
    // }

    @Override
    public <T> Collection<T> parallelChunks(Map args, Collection<T> collection,
                                            @ClosureParams(SecondParam) Closure closure) {
        int batchSize = args.batchSize ? args.batchSize as Integer : getBatchSize()

        def batchedList = collate(collection, batchSize)

        parallel(args, batchedList, closure)

        return collection
    }

    @Override
    public <T> Collection<T> eachParallel(Map args, Collection<T> collection,
                                          @ClosureParams(SecondParam.FirstGenericType) Closure closure){
        int psize = args.poolSize ? args.poolSize as Integer : getPoolSize()
        withPool(psize) {
            GParsPoolUtil.eachParallel(collection, wrapSession(args, closure))
        }
        return collection
    }


}
