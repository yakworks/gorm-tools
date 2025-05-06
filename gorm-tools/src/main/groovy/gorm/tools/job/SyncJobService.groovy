/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.transaction.TrxService
import yakworks.api.ApiResults
import yakworks.commons.lang.Validate
import yakworks.i18n.icu.ICUMessageSource
import yakworks.json.groovy.JsonEngine
import yakworks.json.groovy.JsonStreaming
import yakworks.spring.AppCtx

/** Trait for a conretete SyncJobService that provides standard functionality to create and update a jobs status */
@CompileStatic
abstract class SyncJobService<D> {
    final static Logger LOG = LoggerFactory.getLogger(SyncJobService)

    @Autowired
    ICUMessageSource messageSource

    @Autowired
    TrxService trxService

    @Autowired
    ProblemHandler problemHandler

    @Autowired
    AsyncService asyncService

    /**
     * get the repo so the synJob
     */
    abstract GormRepo<D> getRepo()

    /**
     * creates and saves the Job and returns the SyncJobContext with the jobId
     */
    SyncJobContext initContext(SyncJobArgs args, Object payload){
        SyncJobContext jobContext = SyncJobContext.of(args).syncJobService(this).payload(payload)
        jobContext.setPayloadSize(payload)

        jobContext.results = ApiResults.create()
        jobContext.startTime = System.currentTimeMillis()
        return jobContext
    }

    /**
     * Creates a SyncJob with SyncJobState.Queued and fires event.
     * @param args SyncJobArgs
     * @return the created SyncJobEntity
     */
    SyncJobEntity queueJob(SyncJobArgs args) {
        args.jobId = ((IdGeneratorRepo)getRepo()).generateId()

        Map data = [
            id: args.jobId,
            source: args.source,
            sourceId: args.sourceId,
            state: SyncJobState.Queued,
            params: args.params
        ] as Map<String,Object>

        if(args.payloadId) {
            //if payloadId, then probably attachmentId with csv for example. Just store it and dont do payload conversion
            data.payloadId = args.payloadId
        }
        else if(args.payload){
            // When collection then check size and set args
            if(args.payload instanceof Collection && ((Collection)args.payload).size() > 1000) {
                //if list if over 1000 then save both as file and not in column
                args.savePayloadAsFile = true
                args.saveDataAsFile = true
            }
            //savePayload is true by default
            if(args.savePayload){
                if (args.payload instanceof Collection && args.savePayloadAsFile) {
                    data.payloadId = writePayloadFile(args.jobId, args.payload as Collection)
                }
                else {
                    String res = JsonEngine.toJson(args.payload)
                    data.payloadBytes = res.bytes
                }
            }
        }

        //the call to this createJob method is already wrapped in a new trx
        SyncJobEntity jobEntity = getRepo().create(data, [flush: true, bindId: true]) as SyncJobEntity
        return jobEntity
    }


    /**
     * creates and saves the Job and returns the SyncJobContext with the jobId
     */
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        SyncJobContext jobContext
        //keep it in its own transaction so it doesn't depend on wrapping
        trxService.withNewTrx {
            jobContext = SyncJobContext.of(args).syncJobService(this).payload(payload)
            //jobContext.createJob()
            doCreateJob(jobContext)
            jobContext.results = ApiResults.create()
            jobContext.startTime = System.currentTimeMillis()
        }
        AppCtx.publishEvent(SyncJobStartEvent.of(jobContext))
        return jobContext
    }

    /** create a job using the syncJobService.repo.create */
    protected SyncJobEntity doCreateJob(SyncJobContext jobContext){
        Object payload = jobContext.payload
        Validate.notNull(jobContext.payload)
        SyncJobArgs syncJobArgs = jobContext.args
        //get jobId early so it can be used, might not need this anymore
        syncJobArgs.jobId = ((IdGeneratorRepo)getRepo()).generateId()
        jobContext.setPayloadSize(payload)

        Map data = [
            id: syncJobArgs.jobId,
            source: syncJobArgs.source,
            sourceId: syncJobArgs.sourceId,
            state: SyncJobState.Running,
            payload: payload //XXX we were adding payload here despite the savePayload, what are the implications?
        ] as Map<String,Object>

        if(payload instanceof Collection && payload.size() > 1000) {
            syncJobArgs.savePayloadAsFile = true
            syncJobArgs.saveDataAsFile = true
        }

        if(syncJobArgs.savePayload){
            if (payload && syncJobArgs.savePayloadAsFile) {
                data.payloadId = writePayloadFile(syncJobArgs.jobId, payload as Collection)
            }
            else {
                String res = JsonEngine.toJson(payload)
                data.payloadBytes = res.bytes
            }
        }

        //the call to this createJob method is already wrapped in a new trx
        SyncJobEntity jobEntity = getRepo().create(data, [flush: true, bindId: true]) as SyncJobEntity

        return jobEntity
    }

    /**
     * Calls the repo update wrapped in a new trx
     */
    SyncJobEntity updateJob(Map data){
        SyncJobEntity sje
        try{
            //keep it in its own transaction so it doesn't depend on and existing. Should be on its own
            trxService.withNewTrx {
                getRepo().clear() //clear so doesn't pull from cache and we dont get optimistic error
                sje = getRepo().update(data, [flush: true]) as SyncJobEntity
            }
        } catch (e){
            LOG.error("Critical error updating SyncJob", e)
            throw e
        }
        return sje
    }

    /**
     * gets the job from the repo
     */
    SyncJobEntity getJob(Serializable id){
        return getRepo().getWithTrx(id) as SyncJobEntity
    }

    /**
     * Creates a nio path file for the id passed in.
     * Will be "${tempDir}/SyncJobData${id}.json".
     * For large bulk operations data results should be stored as attachment file
     *
     * @param filename the temp filename to use
     * @return the Path object to use
     */
    abstract Path createTempFile(String filename)

    /**
     * create attachment
     *
     * @param params the paramater to pass to the attachment creation
     * @return the attachmentId
     */
    abstract Long createAttachment(Path path, String name)

    /**
     * Standard pattern to run a function asynchrons (assuming asyncArgs is setup that way).
     * Will call the finish job when its done.  Supplier can be anything really.
     * @param asyncArgs the async args to pass to supplyAsync
     * @param jobContext the active jobContext
     * @param runnable the runnable function to run
     * @return the job id from the jobContext.jobId
     */
    Long runJob(AsyncArgs asyncArgs, SyncJobContext jobContext, Runnable runnable) {
        //process each glbatch in async
        asyncService
            .supplyAsync (asyncArgs, () -> runnable.run()) //FIXME we really should be using runAsync as we do nothing with what the supplier returns
            .whenComplete { res, ex ->
                if (ex) {
                    //ideally should not happen as the pattern here is that all exceptions should be handled in supplierFunc
                    jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
                }
                jobContext.finishJob()
            }

        return jobContext.jobId
    }

    /**
     * Standard pattern to run a function asynchrons (assuming asyncArgs is setup that way).
     * Will call the finish job when its done.  Supplier can be anything really.
     * @param asyncArgs the async args to pass to supplyAsync
     * @param jobContext the active jobContext
     * @param runnable the runnable function to run
     * @return the job id from the jobContext.jobId
     */
    Long runJob(SyncJobContext jobContext, Runnable runnable) {
        return runJob(jobContext.args.asyncArgs, jobContext, runnable)
    }

    /**
     * when args.savePayload and args.savePayloadAsFile are true, this is called to save the payload to file
     * @param payload the payload List or Map that was sent (will normally have been json when called via REST
     * @return the Attachment id.
     */
    protected Long writePayloadFile(Serializable jobId, Collection payload){
        String filename = "SyncJobPayload_${jobId}_.json"
        Path path = createTempFile(filename)
        JsonStreaming.streamToFile(payload, path)
        return createAttachment(path, filename)
    }
}
