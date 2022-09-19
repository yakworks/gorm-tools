/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async


import groovy.transform.CompileStatic
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SecondParam

import org.grails.datastore.mapping.core.Datastore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionSynchronizationManager

import gorm.tools.config.AsyncConfig
import yakworks.commons.lang.Validate

/**
 * a trait to be used for colating/slicing a list into "batches" to then asynchronously with Transactions
 * insert or update for maximum performance.
 *
 * @see ParallelStreamTools  - for a concrete implementation
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait ParallelTools {
    final static Logger LOG = LoggerFactory.getLogger(ParallelTools)

    @Autowired
    AsyncConfig asyncConfig

    @Autowired
    AsyncService asyncService

    /**
     * Iterates over the lists and calls the closure passing in the list item and the args asynchronously.
     * see GParsPoolUtil.eachParallel. This will check if asyncEnabled is true by default or in args
     * and if its false then it just gracefully passes the closure on to the collection.each
     *
     * Must be overriden by the concrete implementation as this is where the meat is.
     *
     * @param asyncArgs looks at poolSize and asyncEnabled
     * @param asyncArgs the collection to iterate process
     * @param closure the closure to call for each item in collection, get the entry from the collection passed to it like norma groovy each
     */
    abstract <T> Collection<T> each(AsyncArgs asyncArgs, Collection<T> collection, Closure closure)


    public <T> Collection<T> each(Collection<T> collection, Closure closure){
        each(new AsyncArgs(), collection, closure)
    }

    /**
     * collates or slices the data collection into slices/chunks and calls the sliceClosure for each slice of items in the item list.
     * Will check to see if asyncEnabled is true and if not then will just call a normal each and pass to chunkClosure
     *
     * @param asyncArgs the async args
     * @param data the items list to slice into chunks
     * @param sliceClosure the closure to call for each collated slice of data. will get passed the List for processing
     */
    public <T> Collection<T> eachSlice(AsyncArgs asyncArgs, Collection<T> data,
                                       @ClosureParams(SecondParam) Closure sliceClosure) {
        Integer sliceSize = asyncArgs.sliceSize ?: asyncConfig.sliceSize

        def slicedList = slice(data, sliceSize)

        each(asyncArgs, slicedList, sliceClosure)

        return data
    }

    public <T> Collection<T> eachSlice(Collection<T> data,
                                       @ClosureParams(SecondParam) Closure sliceClosure){
        eachSlice(new AsyncArgs(), data,  sliceClosure)
    }

    /**
     * Will slice the data based on args or defaults and then process by slices.
     * this differs from the eachSlice in that the itemClosure gets called for each item in the collection
     * like a normal groovy .each where the closure passed to eachSlice gets called for each slice or chunk or data.
     *
     * pass transactional = true to have each slice be in its own transaction as you normally would for eachSlice
     *
     * @param asyncArgs the arguments for how the asyn should be setup
     * @param list the list to process that will get sliced into batches via collate
     * @param itemClosure the closure to pass to eachParallel that gets passed each entry in the collection
     */
    public <T> Collection<T> slicedEach(AsyncArgs asyncArgs, Collection<T> collection,
                                        @ClosureParams(SecondParam.FirstGenericType) Closure itemClosure) {

        getAsyncService().verifyDatastore(asyncArgs)
        Closure sliceClos = sliceClosure(asyncArgs.datastore, itemClosure)

        eachSlice(asyncArgs, collection as Collection<Object>, sliceClos) as Collection<T>

    }


    /**
     * Uses collate to slice the list into sub-lists. Uses the asyncService.sliceSize unless batchSize is sent in
     * @param items the list to slice up into batches
     * @param sliceSize _optional_ the length of each batch sub-list in the returned list. override the asyncService.sliceSize
     * @return the (list of lists) containing the chunked list of items
     */
    public <T> List<List<T>> slice(Collection<T> items, Integer sliceSize = null) {
        items.collate(sliceSize ?: asyncConfig.sliceSize)
    }

    /**
     * returns closure that can be passed to an '.each' for a slice of data
     * the itemClosure is called  for each row in the slice and pass the item
     * if its in a transaction then will flush and clear when done
     */
    Closure sliceClosure(Datastore ds, Closure itemClosure) {
        Validate.notNull(ds, '[datastore]')
        return { Collection sliceOfitems ->
            sliceOfitems.each(itemClosure)
            if (TransactionSynchronizationManager.isSynchronizationActive()){
                ds.currentSession.flush()
                ds.currentSession.clear()
            }
        }
    }

    /**
     * sliceClosure that uses default datastore from trxService
     */
    Closure sliceClosure(Closure itemClosure) {
        Datastore dstore = getAsyncService().trxService.targetDatastore
        sliceClosure(dstore, itemClosure)
    }
}
