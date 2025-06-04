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
import org.springframework.transaction.annotation.Propagation

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import gorm.tools.job.events.SyncJobQueueEvent
import gorm.tools.job.events.SyncJobStateEvent
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.transaction.TrxService
import grails.gorm.transactions.Transactional
import yakworks.api.ApiResults
import yakworks.i18n.icu.ICUMessageSource
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
     * Update state to RUNNING
     * Initialize the SyncJobContext and return it
     */
    SyncJobContext startJob(SyncJobEntity job, SyncJobArgs syncJobArgs){
        assert job.state == SyncJobState.Queued
        job = changeJobStatusToRunning(job.id)
        assert job.state == SyncJobState.Running
        //make sure args has jobId
        syncJobArgs.jobId = job.id

        SyncJobContext jobContext = SyncJobContext.of(syncJobArgs).syncJobService(this)
        jobContext.results = ApiResults.create()
        jobContext.startTime = System.currentTimeMillis()

        AppCtx.publishEvent(SyncJobStateEvent.of(job.id, jobContext, job.state))
        return jobContext
    }

    /**
     * Call repo create in new trx and fires SyncJobQueueEvent.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    SyncJobEntity queueJob(Map data){
        if(!data.id) data.id = generateId()
        if(!data.state) data.state = SyncJobState.Queued
        SyncJobEntity jobEntity = getRepo().create(data, PersistArgs.of(flush: true, bindId: true)) as SyncJobEntity
        //NOTE: The event listener is where its either picked up and run or it put on hazelcast queue to be picked up and run
        AppCtx.publishEvent(new SyncJobQueueEvent(jobEntity))
        return jobEntity
    }

    Long generateId(){
        ((IdGeneratorRepo)getRepo()).generateId()
    }
    /**
     * creates and saves a Running Job and returns the SyncJobContext with the jobId
     */
    @Deprecated //legacy
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        //set the payload if not already
        if(!args.payload) args.payload = payload
        // new way
        var jobDta = args.asJobData()
        SyncJobEntity syncJobEntity = queueJob(jobDta)
        SyncJobContext jobContext = startJob(syncJobEntity, args)
        //set payload size for messaging progress
        if(payload instanceof Collection) jobContext.payloadSize = payload.size()

        return jobContext
    }

    /**
     * Calls the repo update wrapped in a new trx
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    SyncJobEntity updateJob(Map data){
        try {
            //XXX @SUD not sure what this was for, seems dangerous to clear cache in middle of run
            getRepo().clear() //clear so doesn't pull from cache and we dont get optimistic error
            return getRepo().update(data, [flush: true]) as SyncJobEntity
        } catch (e){
            LOG.error("Critical error updating SyncJob", e)
            throw e
        }
    }

    /**
     * Changes job state to Running before starting bulk export job
     */
    SyncJobEntity changeJobStatusToRunning(Serializable jobId) {
        updateJob([id:jobId, state: SyncJobState.Running])
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
    @Deprecated
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
     * when args.savePayloadAsFile are true, this is called to save the payload to file
     * @param payload the payload List or Map that was sent (will normally have been json when called via REST
     * @return the Attachment id.
     */
    Long writePayloadFile(Serializable jobId, Collection payload){
        String filename = "SyncJobPayload_${jobId}_.json"
        Path path = createTempFile(filename)
        JsonStreaming.streamToFile(payload, path)
        return createAttachment(path, filename)
    }
}
