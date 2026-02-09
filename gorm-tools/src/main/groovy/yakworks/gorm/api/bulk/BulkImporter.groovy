/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.async.AsyncArgs
import gorm.tools.async.ParallelTools
import gorm.tools.job.SyncJobContext
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.events.AfterBulkSaveEntityEvent
import gorm.tools.repository.events.BeforeBulkSaveEntityEvent
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.DataOp
import gorm.tools.repository.model.EntityResult
import gorm.tools.utils.ServiceLookup
import yakworks.api.ApiResults
import yakworks.api.HttpStatus
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.lang.Validate
import yakworks.commons.map.LazyPathKeyMap
import yakworks.commons.map.Maps
import yakworks.meta.MetaMap
import yakworks.spring.AppCtx

/**
 * Core functionality for slicing and insert/update or upsert a list of maps
 * Primarily used in BulkImportService
 */
@SuppressWarnings(["Println"])
@Slf4j
@CompileStatic
class BulkImporter<D> {

    @Autowired
    @Qualifier("parallelTools")
    ParallelTools parallelTools

    @Autowired
    MetaMapService metaMapService

    @Autowired
    ProblemHandler problemHandler

    @Autowired
    RepoEventPublisher repoEventPublisher

    Class<D> entityClass // the domain class this is for

    BulkImporter(Class<D> entityClass){
        this.entityClass = entityClass
    }

    static <D> BulkImporter<D> lookup(Class<D> entityClass){
        ServiceLookup.lookup(entityClass, BulkImporter<D>, "defaultBulkImporter")
    }

    GormRepo<D> getRepo(){
        return RepoLookup.findRepo(entityClass)
    }

    /**
     * wrap doBulkParallel and calls bulk
     *
     * @param dataList the list of data maps to create
     * @param jobContext the jobContext for the job
     * @return the id, just whats in jobContext
     */
    Long bulkImport(List<Map> dataList, SyncJobContext jobContext) {
        //If dataList is empty then error right away.
        if(dataList == null || dataList.isEmpty())
            throw DataProblem.of('error.data.emptyPayload').detail("Bulk Data is Empty").toException()

        //make sure it has a job here.
        Validate.notNull(jobContext.jobId)

        try {
            //jobContext.args.entityClass = getEntityClass()
            doBulkParallel(dataList, jobContext)
        } catch (ex) {
            //ideally should not happen as the pattern here is that all exceptions should be handled in doBulkParallel
            log.error("Unhandled exception while running bulkImport")
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
        finally {
            jobContext.finishJob()
            //fire finished event
            BulkImportFinishedEvent<D> evt = new BulkImportFinishedEvent(jobContext, (BulkImportJobArgs)jobContext.args, entityClass)
            AppCtx.publishEvent(evt)
        }

        return jobContext.jobId
    }

    void doBulkParallel(List<Map> dataList, SyncJobContext jobContext){
        List<Collection<Map>> sliceErrors = Collections.synchronizedList([] as List<Collection<Map>> )
        BulkImportJobArgs jobArgs = (BulkImportJobArgs)jobContext.args
        AsyncArgs pconfig = AsyncArgs.of(getRepo().getDatastore())
        pconfig.enabled = jobArgs.parallel //same as above, ability to override through params
        // wraps the bulkCreateClosure in a transaction, if async is not enabled then it will run single threaded
        parallelTools.eachSlice(pconfig, dataList) { dataSlice ->
            ApiResults results
            try {
                getRepo().withTrx {
                    results = doBulkSlice((List<Map>) dataSlice, jobArgs)
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
            AsyncArgs asynArgsNoTrx = AsyncArgs.of(getRepo().getDatastore())
            asynArgsNoTrx.enabled = jobArgs.parallel
            parallelTools.each(asynArgsNoTrx, sliceErrors) { dataSlice ->
                try {
                    ApiResults results = doBulkSlice((List<Map>) dataSlice, jobArgs, true)
                    jobContext.updateJobResults(results, false)
                } catch(Exception ex) {
                    log.error("Unhandled exception while running doBulkParallel")
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
    protected ApiResults doBulkSlice(List<Map> dataList, BulkImportJobArgs jobArgs, boolean transactionPerItem = false){
        // println "will do ${dataList.size()}"
        ApiResults results = ApiResults.create(false)
        for (Map item : dataList) {
            try {
                EntityResult<Map> entResult = bulkSaveEntity(item, jobArgs, transactionPerItem)
                results << Result.OK().payload(entResult.entity).status(entResult.status)
            } catch(Exception e) {
                // if trx by item then collect the exceptions, otherwise throw so it can rollback
                if(transactionPerItem){
                    results << problemHandler.handleException(e).payload(buildErrorMap(item, jobArgs.errorIncludes))
                } else {
                    getRepo().clear() //clear cache on error since wont hit below
                    throw e
                }
            }
        }
        // flush and clear here so easier to debug problems and clear for memory to help garbage collection
        if(getRepo().getDatastore().hasCurrentSession()) {
            // if trx is per item then it already flushes at trx commit so only clear.
            transactionPerItem ? getRepo().clear() : getRepo().flushAndClear()
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
    protected EntityResult<Map> bulkSaveEntity(Map data, BulkImportJobArgs jobArgs, boolean transactional) {
        //need to copy the incoming map, as during create(), repos may remove entries from the data map
        //or it can create circular references - eg org.contact.org - which would result in Stackoverflow when converting to json
        Map dataClone

        if(data instanceof LazyPathKeyMap){
            dataClone = data.cloneMap()  //clone it, probably from CSV
        } else {
            dataClone = Maps.clone(data)
        }

        def closure = {
            doBeforeBulkSaveEntity(dataClone, jobArgs)
            PersistArgs pargs = jobArgs.persistArgs ? jobArgs.persistArgs.clone() : PersistArgs.of()
            D entityInstance
            DataOp op = jobArgs.op
            int statusCode
            if(op == DataOp.add) { // create
                entityInstance = getRepo().doCreate(dataClone, pargs)
                statusCode = HttpStatus.CREATED.code
            } else if (op == DataOp.update) { // update
                entityInstance = getRepo().doUpdate(dataClone, pargs)
                statusCode = HttpStatus.OK.code
            } else if (op == DataOp.upsert) { // upsert (insert or update)
                EntityResult res = getRepo().upsert(dataClone, pargs)
                entityInstance = res.entity
                statusCode = res.status.code
            } else {
                throw new UnsupportedOperationException("DataOp $op not supported")
            }
            doAfterBulkSaveEntity(entityInstance, dataClone, jobArgs)
            Map successMap = buildSuccessMap(entityInstance, jobArgs.includes)
            return EntityResult.of(successMap).status(statusCode)
        } as Closure<EntityResult<Map>>

        return transactional ? getRepo().withTrx(closure) : closure()
    }

    /**
     * creates response map based on bulk include list
     * this is called instead of just createMetaMap so it can be overriden easily in implementations.
     */
    protected Map buildSuccessMap(D entityInstance, List<String> includes) {
        return createMetaMap(entityInstance, includes)
    }

    /**
     * The fields to return when bulk fails for the entity, by default, return entire incoming map back.
     */
    protected Map buildErrorMap(Map originalData, List<String> errorIncludes) {
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
    protected Map createMetaMap(D entityInstance, List<String> includes){
        MetaMap entityMapData = metaMapService.createMetaMap(entityInstance, includes)
        return (Map)entityMapData.clone()
    }

    /**
     * Called from BulkSaveEntity before doCreate/doUpdate.
     * Gives an oportunity to modify the data with any special changes needed in a bulk op.
     * will be inside the trx if one is created, so can throw an error if needing to reject the save.
     */
    protected void doBeforeBulkSaveEntity(Map data, BulkImportJobArgs jobArgs) {
        BeforeBulkSaveEntityEvent<D> event = new BeforeBulkSaveEntityEvent<D>(getRepo(), data, jobArgs)
        repoEventPublisher.publishEvents(getRepo(), event, [event] as Object[])
    }

    /**
     * Called after the doupdate or create has been called for each item.
     */
    protected void doAfterBulkSaveEntity(D entity, Map data, BulkImportJobArgs jobArgs) {
        AfterBulkSaveEntityEvent<D> event = new AfterBulkSaveEntityEvent<D>(getRepo(), entity, data, jobArgs)
        repoEventPublisher.publishEvents(getRepo() , event, [event] as Object[])
    }

}
