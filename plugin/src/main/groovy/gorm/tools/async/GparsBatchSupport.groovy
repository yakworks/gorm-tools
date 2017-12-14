package gorm.tools.async

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
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
     * Iterates over the batchList with eachParallel and calls the closure passing in the list and the args
     * Generally you will want to use the {@link AsyncBatchSupport#parallel} method
     * that added by the Trait as it calls the withTransaction
     *
     * @param args _optional_ arg map will be passed down through the closure as well <br>
     *     - poolSize : gets passed down into the GParsPool.withPool for
     * @param batchList a collated list of lists. each list in the batchList will be asynchronously passed to the provided closure
     * @param closure the closure to call on each list in the batchList
     */
    @Override
    void parallelClosure(Map args, List<List> batchList, Closure clos) {
        int psize = args.poolSize ? args.poolSize as Integer : getPoolSize()
        GParsPool.withPool(psize) {
            GParsPoolUtil.eachParallel(batchList){ List batch ->
                clos(batch, args)
            }
        }
    }

    @Override
    @Transactional
    void withTransaction(Map args, List batch, Closure clos) {
        for (Object item : batch) {
            clos(item, args)
        }
        DaoUtil.flushAndClear()
    }

}
