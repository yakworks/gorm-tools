package gorm.tools.async

import gorm.tools.WithTrx
import groovy.transform.CompileStatic
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.TransactionStatus

/**
 * a trait to be used for colating/slicing a list into "batches" to then asynchronously with Transactions
 * insert or update for maximum performance.
 *
 * @see GparsBatchSupport  GparsBatchSupport - for a concrete implementation
 *
 * @author Joshua Burnett
 * @since 6.1
 */
@CompileStatic
trait AsyncBatchSupport implements WithTrx {

    /** The list size to send to the collate that slices.*/
    @Value('${hibernate.jdbc.batch_size:0}')
    int batchSize

    /** calls {@link #parallel()} and passes args, batchList and the closure through to {@link #batchTrx()} */
    /**
     * calls {@link #parallel} with the args, batches list calling {@link #doBatch} for each batch in batches
     * passes itemClosure down through to the {@link #doBatch}
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     can also add any other value and they will be passed down through the closure as well <br>
     *     - poolSize : gets passed down into the GParsPool.withPool for example
     * @param batches a collated list(batches) of sub-lists(a batch or items). each item in the batch(sub-list)
     *   will be asynchronously passed to the provided itemClosure. You should first collate the list with {@link #collate}
     * @param itemClosure the closure to call for each item lowest list.
     */
    void parallelBatch(Map args = [:], List<List> batches, Closure itemClosure) {
        parallel(args, batches) { List batch, Map cargs ->
            batchTrx(cargs, batch, itemClosure)
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
     * @param batches a collated list of lists. each batch list in the batches will be asynchronously passed to the provided closure
     * @param batchClosure the closure to call for each batch(sub-list of items) in the batches(list of batch sub-lists)
     */
    abstract void parallel(Map args = [:], List<List> batches, Closure batchClosure)

    /**
     * Uses collate to break or slice the list into batches and then calls parallelBatch
     *
     * @param args optional arg map to be passed on through to eachParallel and the async engine such as gpars. <br>
     *     - batchSize : parameter to be passed into collate
     * @param list the list to process that will get sliced into batches via collate
     * @param itemClosure the closure to pass to eachParallel which is then passed to withTransaction
     */
    void parallelCollate(Map args = [:], List list, Closure itemClosure) {
        parallelBatch(args, collate(list, args.batchSize as Integer), itemClosure)
    }

    /**
     * Uses collate to break or slice the list into sub-lists of batches. Uses the {@link #batchSize} unless listSize is sent in
     * @param items the list to slice up into batches
     * @param batchSize _optional_ the length of each batch sub-list in the returned list. override the {@link #batchSize}
     * @return the batches (list of lists) containing the chunked list of items
     */
    List<List> collate(List items, Integer batchSize = null) {
        items.collate(batchSize ?: getBatchSize())
    }

    /**
     * runs {@link #doBatch ) in a Transaction and flush and clear is called after doBatch but before commit.
     */
    void batchTrx(Map args, List items, Closure itemClosure) {
        withTrx { TransactionStatus status ->
            doBatch(args, items, itemClosure)
            //status.flush()
            //clear(status)
            flushAndClear(status)
        }

    }

    /**
     * calls closure for each item in list. flush and clear is called after all items are done
     * and just before commit.
     *
     * @param args <i>optional args to pass to the closure. coming from parallelClosure
     * @param items the list or items to iterate over and run the closure
     * @param itemClosure the closure to execute for each item. will get passed the item and args as it itereates over the list
     */
    void doBatch(Map args, List items, Closure itemClosure) {
        for (Object item : items) {
            itemClosure.call(item, args)
        }
    }
}
