/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SecondParam

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.TransactionStatus

import gorm.tools.transaction.WithTrx
import grails.persistence.support.PersistenceContextInterceptor

/**
 * a trait to be used for colating/slicing a list into "batches" to then asynchronously with Transactions
 * insert or update for maximum performance.
 *
 * @see GparsAsyncSupport  GparsAsyncSupport - for a concrete implementation
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait AsyncSupport implements WithTrx {
    final static Logger LOG = LoggerFactory.getLogger(AsyncSupport)

    /** The list size to send to the collate that slices.*/
    @Value('${hibernate.jdbc.batch_size:0}')
    int batchSize

    /** The list size to send to the collate that slices.*/
    @Value('${gorm.tools.async.enabled:false}')
    boolean asyncEnabled

    @Autowired
    PersistenceContextInterceptor persistenceInterceptor

    /**
     * main difference here from {@link #eachParallel} eachParallel is that this will check if asyncEnabled is true
     * if its false then it just gracefully passes the closure on to the colection.each
     *
     * Must be overriden by the concrete implementation as this is where the meat is.
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     - poolSize : gets passed down into the GParsPool.withPool for example
     *     - asyncEnabled : forces async to true of false regardless of what the class val is, useful for testing
     * @param collection the collection to iterate process
     * @param closure the closure to call for each item in collection, get the entry from the collection passed to it like norma groovy each
     */
    abstract void parallel(Map args = [:], Collection collection, Closure closure)


    // abstract void parallel(Map args, List<List> collection, Closure closure)

    /**
     * collates the data into slices/chunks and calls the chunkClosure for each slice of items in the item list.
     * Will check to see if asyncEnabled is true and if not then will just call a normal each and pass to chunkClosure
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     - batchSize : the size of the lists when collated or sliced into chunks
     * @param collection the items list to slice into chunks
     * @param chunkClosure the closure to call for each collated slice of data. will get passed the List for processing
     */
    abstract <T> Collection<T> parallelChunks(Map args = [:], Collection<T> collection,
                                              @ClosureParams(SecondParam) Closure closure)

    /**
     * Iterates over the lists and calls the closure passing in the list item and the args asynchronously.
     * see GParsPoolUtil.eachParallel
     * Must be overriden by the concrete implementation as this is where the meat is.
     *
     * @param args _optional_ arg map to be passed to the async engine such as gpars.
     *     can also add any other value and they will be passed down through the closure as well <br>
     *     - poolSize : gets passed down into the GParsPool.withPool for example
     * @param collection the collection to iterate process
     * @param closure the closure to call for each item in collection, get the entry from the collection passed to it like norma groovy each
     */
    abstract <T> Collection<T> eachParallel(Map args = [:], Collection<T> collection,
                                            @ClosureParams(SecondParam.FirstGenericType) Closure closure)

    /**
     * Uses collate to break or slice the list into batches and then process the each(itemClosure) inside a trx
     * here more for example, not normally something we would run in production
     *
     * @param args optional arg map to be passed on through to eachParallel and the async engine such as gpars. <br>
     *     - batchSize : parameter to be passed into collate
     * @param list the list to process that will get sliced into batches via collate
     * @param itemClosure the closure to pass to eachParallel that gets passed the entry in the collection
     */
    public <T> Collection<T> parallelCollate(Map args = [:], Collection<T> collection,
                                             @ClosureParams(SecondParam.FirstGenericType) Closure closure) {

        // List<List<T>> collated = collate(collection, args.batchSize as Integer)
        parallelChunks(args as Map, collection as Collection<Object>) { batch ->
            batchTrx(batch, closure)
        } as Collection<T>

    }

    /**
     * Uses collate to break or slice the list into sub-lists of batches. Uses the {@link #batchSize} unless batchSize is sent in
     * @param items the list to slice up into batches
     * @param batchSize _optional_ the length of each batch sub-list in the returned list. override the {@link #batchSize}
     * @return the batches (list of lists) containing the chunked list of items
     */
    public <T> List<List<T>> collate(Collection<T> items, Integer batchSize = null) {
        items.collate(batchSize ?: getBatchSize())
    }

    /**
     * runs in a Transaction and flush and clear is called after doBatch but before commit.
     */
    void batchTrx(Collection items, Closure itemClosure) {
        withTrx { TransactionStatus status ->
            items.each(itemClosure)
            flushAndClear(status)
        }
    }

    public Closure withEachTrx(Closure wrapped) {
        return { items ->
            withTrx { TransactionStatus status ->
                items.each(wrapped)
                flushAndClear(status)
            }
        }
    }

    /**
     * if args has a withSession: true then wrap in a session
     */
    public <T> Closure<T> wrapSession(Map args = [:], Closure<T> c) {
        return { T item ->
            if(args.withSession) {
                withSession { session ->
                    c.call(item)
                }
            } else {
                c.call(item)
            }
        }
    }
    //
    // public <T> Closure<T> withSession(Closure<T> wrapped) {
    //     return { T item ->
    //         persistenceInterceptor.init()
    //         try {
    //             wrapped.call(item)
    //         } finally {
    //             try {
    //                 persistenceInterceptor.flush()
    //                 persistenceInterceptor.clear()
    //             } catch (Exception e) {
    //                 LOG.error("unexpected fail to flush and clear session after withSession", e);
    //             } finally {
    //                 try {
    //                     persistenceInterceptor.destroy()
    //                 } catch (Exception e) {
    //                     LOG.error("unexpected fail to flush and clear session after withSession", e);
    //                 }
    //             }
    //         }
    //     }
    // }
}
