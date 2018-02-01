package gorm.tools.async

import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovyx.gpars.util.PoolUtils
import org.springframework.beans.factory.annotation.Value

import javax.annotation.PostConstruct

/**
 * a Gpars implementation of the AsyncBatchSupport trait
 * to be used for colating/slicing a list into "batches" to then asynchronously process with Transactions
 * insert or update for maximum performance.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class GparsBatchSupport implements AsyncBatchSupport {

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

    /**
     * Iterates over the batchList with GParsPoolUtil.eachParallel and calls the closure passing in the list and the args
     * Generally you will want to use the {@link AsyncBatchSupport#parallel} method
     * that added by the Trait as it calls the withTransaction
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     can also add any other value and they will be passed down through the closure as well <br>
     *     - poolSize : gets passed down into the GParsPool.withPool for example
     * @param batches a collated list of lists. each batch list in the batches will be asynchronously passed to the provided closure
     * @param batchClosure the closure to call for each batch(sub-list of items) in the batches(list of batch sub-lists)
     */
    @Override
    void parallel(Map args, List<List> batches, Closure batchClosure) {
        int psize = args.poolSize ? args.poolSize as Integer : getPoolSize()
        GParsPool.withPool(psize) {
            GParsPoolUtil.eachParallel(batches) { List batch ->
                batchClosure.call(batch, args.clone())
            }
        }
    }

}
