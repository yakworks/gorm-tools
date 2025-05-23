/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path
import java.util.function.Supplier

import groovy.transform.CompileStatic

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.async.AsyncArgs
import gorm.tools.async.AsyncService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.transaction.TrxService
import yakworks.i18n.icu.ICUMessageSource
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
    SyncJobContext createJob(SyncJobArgs args, Object payload){
        SyncJobContext jobContext
        //keep it in its own transaction so it doesn't depend on wrapping
        trxService.withNewTrx {
            jobContext = SyncJobContext.of(args).syncJobService(this).payload(payload)
            jobContext.createJob()
            jobContext.startTime = System.currentTimeMillis()
        }
        AppCtx.publishEvent(SyncJobStartEvent.of(jobContext))
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

}
