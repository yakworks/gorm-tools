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
import yakworks.i18n.icu.ICUMessageSource
import yakworks.json.groovy.JsonEngine
import yakworks.json.groovy.JsonStreaming
import yakworks.spring.AppCtx

/** Abstract base class for a conretete SyncJobService that provides standard functionality to create and update a jobs status */
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
    SyncJobEntity queueJob(SyncJobArgs args, SyncJobState state = SyncJobState.Queued) {
        args.jobId = ((IdGeneratorRepo)getRepo()).generateId()

        Map data = [
            id: args.jobId,
            source: args.source,
            sourceId: args.sourceId,
            state: state,
            params: args.params,
            jobType: args.jobType
        ] as Map<String,Object>

        if(args.payloadId) {
            //if payloadId, then probably attachmentId with csv for example. Just store it and dont do payload conversion
            data.payloadId = args.payloadId
        }
        else if(args.payload){
            //savePayload is true by default
            if(args.savePayload){
                if (args.isSavePayloadAsFile()) {
                    data.payloadId = writePayloadFile(args.jobId, args.payload as Collection)
                }
                else {
                    String res = JsonEngine.toJson(args.payload)
                    data.payloadBytes = res.bytes
                }
            }
        }
        //create is transactional
        SyncJobEntity jobEntity

        trxService.withNewTrx {
            jobEntity = getRepo().create(data, [flush: true, bindId: true]) as SyncJobEntity
        }
        //NOTE: The event listener is where its either picked up and run or it put on hazelcast queue to be picked up and run
        AppCtx.publishEvent(new SyncJobQueueEvent(jobEntity, args))

        return jobEntity
    }


    /**
     * creates and saves a Running Job and returns the SyncJobContext with the jobId
     */
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        //set the payload if not already
        if(!args.payload) args.payload = payload
        //jobContext.createJob()
        SyncJobEntity syncJobEntity = queueJob(args, SyncJobState.Running)

        SyncJobContext jobContext = SyncJobContext.of(args).syncJobService(this).payload(payload)
        jobContext.results = ApiResults.create()
        jobContext.startTime = System.currentTimeMillis()

        AppCtx.publishEvent(SyncJobStateEvent.of(syncJobEntity.id, jobContext, syncJobEntity.state))
        return jobContext
    }

    /**
     * Calls the repo update wrapped in a new trx
     */
    SyncJobEntity updateJob(Map data){
        SyncJobEntity sje
        try{
            //keep it in its own transaction so it doesn't depend on and existing. Should be on its own
            trxService.withNewTrx {
                //XXX @SUD not sure what this was for, seems dangerous to clear cache in middle of run
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
        //process in async
        asyncService
            .supplyAsync (asyncArgs, () -> runnable.run()) //FIXME we really should be using runAsync as we do nothing with what the supplier returns
            .whenComplete { res, ex ->
                if (ex) {
                    //ideally should not happen as the pattern here is that all exceptions should be handled in supplierFunc
                    LOG.error("Unhandled exception while running job")
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
