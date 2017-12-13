package gorm.tools.async

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovyx.gpars.util.PoolUtils
import org.springframework.beans.factory.annotation.Value

import javax.annotation.PostConstruct

@CompileStatic
class GparsBatchSupport implements AsyncBatchSupport {

    @Value('${gpars.poolsize:0}')
    int poolSize

    @PostConstruct
    void init() {
        if (poolSize == 0) poolSize = PoolUtils.retrieveDefaultPoolSize()
        //if batchSize is 0 then hibernate may not bbe installed and hibernate.jdbc.batch_size is not set. force it to 100
        if (batchSize == 0) batchSize = 100
    }

    //@CompileDynamic
    void parallelClosure(Map args = [:], List<List> batchList, Closure clos) {
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
