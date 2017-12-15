package gorm.tools.async

import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value

/**
 * a trait to be used for colating/slicing a list into "batches" to then asynchronously with Transactions
 * insert or update for maximum performance.
 *
 * @see GparsBatchSupport  GparsBatchSupport - for a concrete implementation
 */
@CompileStatic
trait AsyncBatchSupport {

    /** The list size to send to the collate that slices.*/
    @Value('${hibernate.jdbc.batch_size:0}')
    int batchSize

    /** calls {@link #parallelClosure} and asses closure args,batchList and closure on through to {@link #withTrx}*/
    void parallelWithTrx(Map args = [:], List<List> batchList, Closure closure) {
        parallel(args, batchList){ List batch, Map cargs ->
            withTrx(cargs, batch, closure)
        }
    }

    /**
     * Iterates over the batchList of lists and calls the closure passing in the list and the args asynchronously.
     * parallelClosure it self is not asynchronous and will return once all the items in the batchList are processed.
     * If you want this method itself to be run asynchronous then it can be done in a Promise as outlined in the Grails async docs
     *
     * Must be overriden by the concrete implementation as this is where the meat is.
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     can also add any other value and they will be passed down through the closure as well <br>
     *     - poolSize : gets passed down into the GParsPool.withPool for example
     * @param batchList a collated list of lists. each list in the batchList will be asynchronously passed to the provided closure
     * @param closure the closure to call on each list in the batchList
     */
    abstract void parallel(Map args = [:], List<List> batchList, Closure closure)

    /**
     * Uses collate to break or slice the list into batches and then calls parallel
     *
     * @param args optional arg map to be passed on through to eachParallel and the async engine such as gpars. <br>
     *     - batchSize : parameter to be passed into collate
     * @param list the list to process that will get sliced into batches via collate
     * @param closure the closure to pass to eachParallel which is then passed to withTransaction
     */
    void parallelCollate(Map args = [:], List list, Closure closure) {
        parallelWithTrx(args, collate(list, args.batchSize as Integer), closure)
    }

    /**
     * Uses collate to break or slice the list into sub-lists of batches. Uses the {@link #batchSize} unless listSize is sent in
     * @param list the list to slice up
     * @param listSize _optional_ the length of each batch sub-list in the returned list. override the {@link #batchSize}
     * @return a List containing the data collated into sub-lists batches
     */
    List<List> collate(List list, Integer listSize = null){
        list.collate(listSize ?: getBatchSize())
    }

    /**
     * Should be overriden and annotated with Transactional by the implementing class
     *
     * @param args <i>optional args to pass to the closure. coming from parallelClosure
     * @param list the list to iterate over and run the closure on each item
     * @param closure the closure to execute. will get passed the item and args as it itereates over the list
     */
    void withTrx(Map args, List list, Closure closure) {
        for (Object item : list) {
            closure(item, args)
        }
    }
}
