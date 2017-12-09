package gorm.tools.async

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovyx.gpars.GParsPool
import groovyx.gpars.util.PoolUtils
import org.springframework.beans.factory.annotation.Value

import javax.annotation.PostConstruct

//@CompileStatic
class GparsBatchService implements AsyncBatchProcess{

    @Value('${hibernate.jdbc.batch_size:100}')
    int batchSize

    @Value('${gpars.poolsize:0}')
    int poolSize

    @PostConstruct
    void init(){
        if(poolSize == 0) poolSize = PoolUtils.retrieveDefaultPoolSize()
    }

    void eachCollate(List<Map> batchList, Map args, Closure clos){
        eachParallel(batchList.collate(batchSize), args, clos)
    }

    void eachParallel(Map args = [:], List<List<Map>> batchList, Closure clos) {
        //println "batchList size ${batchList.size()}"
        GParsPool.withPool(args.poolSize?:poolSize) {
            batchList.eachParallel { List<Map> batch ->
                //println "eachParallel batch size ${batch.size()}"
                withTransaction(batch, args, clos)
            }
        }
    }

    @CompileStatic
    @Transactional
    void withTransaction(List<Map> batch,  Map args, Closure clos) {
        for (Map record : batch) {
            clos(record, args)
        }
        DaoUtil.flushAndClear()
    }

}
