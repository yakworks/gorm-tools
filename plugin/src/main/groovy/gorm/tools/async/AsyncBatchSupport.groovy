package gorm.tools.async

import gorm.tools.dao.DaoUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

@CompileStatic
trait AsyncBatchSupport {

    @Value('${hibernate.jdbc.batch_size:0}')
    int batchSize

    /**
     * Iterates over the batchList of lists and process each list slice in withTransaction
     *
     * @param args optional arg map to be passed to the async engine such as gpars.
     *      poolsize : is an example that can be used in gpars
     * @param batchList a collated list of lists. each list in the batchList will be the batched as a Transaction in withTransaction
     * @param clos the closure to pass to withTransaction
     */
    void parallel(Map args = [:], List<List> batchList, Closure closure) {
        parallelClosure(args, batchList){ List batch, Map cargs ->
            withTransaction(cargs, batch, closure)
        }
    }

    /**
     * Iterates over the batchList of lists and calls the closure passing in the list and the args
     *
     * @param args optional args to be passed to the async engine such as gpars. passed into closure as well
     *      poolsize : is an example that can be used in gpars
     * @param batchList a collated list of lists. each list in the batchList will be asyncfnously passed to the provided closure
     * @param clos the closure to call on each list in the slice
     */
    abstract void parallelClosure(Map args, List<List> batchList, Closure closure)
    abstract void parallelClosure(List<List> batchList, Closure closure)

    /**
     * Uses collate to break or slice the list into batches and then runs with eachParralel
     *
     * @param args optional arg map to be passed on through to eachParallel and the async engine such as gpars.
     *      batchSize : parameter to be passed into collate
     * @param list the list to process that will get sliced into batches via collate
     * @param clos the closure to pass to eachParallel which is then passed to withTransaction
     */
    void eachCollate(Map args = [:], List list, Closure clos) {
        parallel(args, list.collate(args.batchSize ? args.batchSize as Integer : getBatchSize()), clos)
    }

    void withTransaction(Map args = [:], List batch, Closure clos) {
        for (Object item : batch) {
            clos(item, args)
        }
    }
}
