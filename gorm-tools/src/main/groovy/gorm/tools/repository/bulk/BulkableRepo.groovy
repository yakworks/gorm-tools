/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.bulk


import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.Datastore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.async.AsyncArgs
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobService
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBulkSaveEntityEvent
import gorm.tools.repository.events.BeforeBulkSaveEntityEvent
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.DataOp
import gorm.tools.repository.model.EntityResult
import yakworks.api.ApiResults
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.map.LazyPathKeyMap
import yakworks.commons.map.Maps
import yakworks.meta.MetaMap

/**
 * A trait that allows to insert or update many (bulk) records<D> at once and create Job <J>
 */
@SuppressWarnings(["Println"])
@CompileStatic
trait BulkableRepo<D> {

    final private static Logger log = LoggerFactory.getLogger(BulkableRepo)

    @Autowired(required = false) //optional to make testing easier and can do gorm-tools without syncJob
    SyncJobService syncJobService

    @Autowired
    @Qualifier("parallelTools")
    ParallelTools parallelTools

    @Autowired
    MetaMapService metaMapService

    @Autowired
    ProblemHandler problemHandler

    //Here for @CompileStatic - GormRepo implements these
    abstract D doCreate(Map data, PersistArgs args)
    abstract D doUpdate(Map data, PersistArgs args)
    abstract EntityResult<D> upsert(Map data, PersistArgs args)
    abstract  Class<D> getEntityClass()
    abstract void flushAndClear()
    abstract void clear()
    abstract Datastore getDatastore()
    abstract RepoEventPublisher getRepoEventPublisher()
    abstract <T> T withTrx(Closure<T> callable)
    abstract <T> T withNewTrx(Closure<T> callable)

    /**
     * creates a supplier to wrap doBulkParallel and calls bulk
     * if syncJobArgs.async = true will return right away
     *
     * @param dataList the list of data maps to create
     * @param syncJobArgs the args object to pass on to doBulk
     * @return Job id
     */
    Long bulk(List<Map> dataList, SyncJobArgs syncJobArgs) {
        //If dataList is empty then error right away.
        if(dataList == null || dataList.isEmpty()) throw DataProblem.of('error.data.emptyPayload').detail("Bulk Data is Empty").toException()

        syncJobArgs.entityClass = getEntityClass()
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, dataList)
        //FIXME why are we setting session: true here? explain. should it be default?
        def asyncArgs = jobContext.args.asyncArgs.session(true)
        // This is the promise call. Will return immediately if syncJobArgs.async=true
        return syncJobService.runJob(asyncArgs, jobContext, () -> doBulkParallel(dataList, jobContext))
    }

    void doBulkParallel(List<Map> dataList, SyncJobContext jobContext){
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )

        AsyncArgs pconfig = AsyncArgs.of(getDatastore())
        pconfig.enabled = jobContext.args.parallel //same as above, ability to override through params
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            ApiResults results
            try {
                withTrx {
                    results = doBulk((List<Map>) dataSlice, jobContext.args)
                }
                if(results?.ok) jobContext.updateJobResults(results, false)
                // ((List<Map>) dataSlice)*.clear() //clear out so mem can be garbage collected
            } catch(Exception e) {
                //on pass1 we collect the slices that failed and will run through them again with each item in its own trx
                sliceErrors.add(dataSlice)
            }

        }

        // if it has slice errors then try again but
        // this time run each item in the slice in its own transaction
        if(sliceErrors.size()) {
            AsyncArgs asynArgsNoTrx = AsyncArgs.of(getDatastore())
            asynArgsNoTrx.enabled = jobContext.args.parallel
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    ApiResults results = doBulk((List<Map>) dataSlice, jobContext.args, true)
                    jobContext.updateJobResults(results, false)
                } catch(Exception ex) {
                    //log.error("BulkableRepo unexpected exception", ex)
                    jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
                }
            }
        }
    }

    /**
     * Does the bulk create/update, normally will be passing in a slice of data.
     * this should be wrapped in a transaction
     * Flushes and clears at the end so errors show up in the right place
     *
     * @param dataList the data chunk
     * @param syncJobArgs the persist args to pass to repo methods
     * @param transactionPerItem default=false which assumes this method is wrapped in a trx and
     *        any error processing the dataList will throw error so it rolls back.
     *        if transactionalItem=true then its assumed that this method is not wrapped in a trx,
     *        and transactionalItem value will be passed to createOrUpdate where each item update or create is in its own trx.
     *        also, if true then this method will try not to throw an exception and
     *        it will collect the errors in the results.
     */
    ApiResults doBulk(List<Map> dataList, SyncJobArgs syncJobArgs, boolean transactionPerItem = false){
        // println "will do ${dataList.size()}"
        ApiResults results = ApiResults.create(false)
        for (Map item : dataList) {
            try {
                EntityResult<Map> entResult = bulkSaveEntity(item, syncJobArgs, transactionPerItem)
                results << Result.OK().payload(entResult.entity).status(entResult.status)
            } catch(Exception e) {
                // if trx by item then collect the exceptions, otherwise throw so it can rollback
                if(transactionPerItem){
                    results << problemHandler.handleException(e).payload(buildErrorMap(item, syncJobArgs.errorIncludes))
                } else {
                    clear() //clear cache on error since wont hit below
                    throw e
                }
            }
        }
        // flush and clear here so easier to debug problems and clear for memory to help garbage collection
        if(getDatastore().hasCurrentSession()) {
            // if trx is per item then it already flushes at trx commit so only clear.
            transactionPerItem ? clear() : flushAndClear()
        }

        return results
    }

    /**
     * Called from doBulk for each item.
     * create or update an entity based on the value in the syncJobArgs.op
     *
     * @param data the item data
     * @param syncJobArgs - persistArgs and isCreate will be pulled from here
     * @param transactional true if should be wraped in withTrx
     * @return the EntityResult with the data map as entity after being run through buildSuccessMap
     *         using the includes in the syncJobArgs
     */
    EntityResult<Map> bulkSaveEntity(Map data, SyncJobArgs syncJobArgs, boolean transactional) {
        //need to copy the incoming map, as during create(), repos may remove entries from the data map
        //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
        Map dataClone

        if(data instanceof LazyPathKeyMap){
            dataClone = data.cloneMap()  //clone it, probably from CSV
        } else {
            dataClone = Maps.clone(data)
        }

        def closure = {
            doBeforeBulkSaveEntity(dataClone, syncJobArgs)
            PersistArgs pargs = syncJobArgs.persistArgs
            D entityInstance
            DataOp op = syncJobArgs.op
            int statusCode
            if(op == DataOp.add) { // create
                entityInstance = doCreate(dataClone, pargs)
                statusCode = HttpStatus.CREATED.code
            } else if (op == DataOp.update) { // update
                entityInstance = doUpdate(dataClone, pargs)
                statusCode = HttpStatus.OK.code
            } else if (op == DataOp.upsert) { // upsert (insert or update)
                EntityResult res = upsert(dataClone, pargs)
                entityInstance = res.entity
                statusCode = res.status.code
            } else {
                throw new UnsupportedOperationException("DataOp $op not supported")
            }
            doAfterBulkSaveEntity(entityInstance, dataClone, syncJobArgs)
            Map successMap = buildSuccessMap(entityInstance, syncJobArgs.includes)
            return EntityResult.of(successMap).status(statusCode)
        } as Closure<EntityResult<Map>>

        return transactional ? withTrx(closure) : closure()
    }

    /**
     * creates response map based on bulk include list
     * this is called instead of just createMetaMap so it can be overriden easily in implementations.
     */
    Map buildSuccessMap(D entityInstance, List<String> includes) {
        return createMetaMap(entityInstance, includes)
    }

    /**
     * The fields to return when bulk fails for the entity, by default, return entire incoming map back.
     */
    Map buildErrorMap(Map originalData, List<String> errorIncludes) {
        if(errorIncludes) {
            return originalData.subMap(errorIncludes)
        } else {
            return originalData
        }
    }

    /**
     * uses metaMapService to create the map for the includes in the jobContext.args
     * Will return a clone to ensure that all properties are called
     * and its a clean, unwrapped, no proxies, map
     */
    Map createMetaMap(D entityInstance, List<String> includes){
        MetaMap entityMapData = metaMapService.createMetaMap(entityInstance, includes)
        return (Map)entityMapData.clone()
    }

    /**
     * Called from BulkSaveEntity before doCreate/doUpdate.
     * Gives an oportunity to modify the data with any special changes needed in a bulk op.
     * will be inside the trx if one is created, so can throw an error if needing to reject the save.
     */
    public void doBeforeBulkSaveEntity(Map data, SyncJobArgs syncJobArgs) {
        GormRepo<D> self = (GormRepo<D>)this
        def event = new BeforeBulkSaveEntityEvent<D>(self, data, syncJobArgs)
        getRepoEventPublisher().publishEvents(self, event, [event] as Object[])
    }

    /**
     * Called after the doupdate or create has been called for each item.
     */
    public <D> void doAfterBulkSaveEntity(D entity, Map data, SyncJobArgs syncJobArgs) {
        GormRepo<D> self = (GormRepo<D>)this
        def event = new AfterBulkSaveEntityEvent<D>(self, entity, data, syncJobArgs)
        getRepoEventPublisher().publishEvents(self, event, [event] as Object[])
    }

    Long bulkExport(SyncJobArgs syncJobArgs) {
        if(syncJobArgs.queryArgs == null) throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()
        syncJobArgs.entityClass = getEntityClass()

        //resulting data should be saved as a file
        syncJobArgs.saveDataAsFile = true
        SyncJobContext jobContext = syncJobService.createJob(syncJobArgs, syncJobArgs.queryArgs.criteriaMap)
        return syncJobService.runJob(syncJobArgs.asyncArgs, jobContext, () -> doBulkExport(jobContext))
    }

    void doBulkExport(SyncJobContext jobContext) {
        try {
            //fetch data list
            //List resultList = query(jobContext.args.queryArgs, null).list()
            //transform result using includes
            //MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
            //Result r = Result.ok().payload(entityMapList)
            //jobContext.updateJobResults(result, false)
        } catch (Exception ex) {
            log.error("BulkableRepo unexpected exception", ex)
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
    }
}
