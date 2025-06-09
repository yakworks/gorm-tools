/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobArgs
import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobService
import gorm.tools.mango.api.QueryArgs
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.transaction.TrxService
import gorm.tools.utils.ServiceLookup
import grails.gorm.transactions.ReadOnly
import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.commons.lang.Validate
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesProps
import yakworks.gorm.config.GormConfig
import yakworks.meta.MetaMapList

@CompileStatic
@Slf4j
class BulkExportService<D> {

    @Autowired SyncJobService syncJobService
    @Autowired MetaMapService metaMapService
    @Autowired ProblemHandler problemHandler
    @Autowired IncludesConfig includesConfig
    @Autowired TrxService trxService
    @Autowired GormConfig gormConfig

    @Value('${yakworks.bulk,export.pageSize:500}')
    Integer pageSize

    Class<D> entityClass // the root domain class this is for

    BulkExportService(Class<D> entityClass){
        this.entityClass = entityClass
    }

    static <D> BulkExportService<D> lookup(Class<D> entityClass){
        ServiceLookup.lookup(entityClass, BulkExportService<D>, "defaultBulkExportService")
    }

    /**
     * Creates a Syncjob and que it up
     */
    SyncJobEntity queueJob(BulkExportJobParams jobParams) {
        //make sure they passed in q or qsearch. queryArgs will have been populated if they did
        if(!jobParams.q) {
            throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()
        }
        //do includes, one of the keys is required. This is not used on queue, only for run.
        if(!jobParams.includes && !jobParams.includesKey) {
            throw DataProblem.ex("includes or includesKey are required params")
        }
        jobParams.entityClassName = getEntityClass().name
        Map data = jobParams.asJobData()
        return syncJobService.queueJob(data)
    }


    /**
     * Starts a bulk import job
     */
    SyncJobEntity runJob(Long jobId) {
        assert jobId
        SyncJobEntity job = syncJobService.getJob(jobId)
        BulkExportJobParams jobParams = BulkExportJobParams.withParams(job.params)
        SyncJobContext jobContext = syncJobService.startJob(job, setupSyncJobArgs(jobParams))
        bulkExport(jobContext)
        return syncJobService.getJob(jobId)
    }


    protected SyncJobArgs setupSyncJobArgs(BulkExportJobParams jobParams){
        SyncJobArgs syncJobArgs = SyncJobArgs.withParams(jobParams.asMap())
        Validate.notEmpty(syncJobArgs.queryArgs) && Validate.isTrue(jobParams.includes || jobParams.includesKey)

        //parse the params into the IncludesProps
        var incProps = new IncludesProps(
            includes: jobParams.includes, includesKey: jobParams.includesKey
        )
        //returns includes if thats passed in or looks up based on includesKey
        syncJobArgs.includes = includesConfig.findIncludes(getEntityClass(), incProps)
        //used for events
        syncJobArgs.entityClass = entityClass
        //force export to Payload on exports
        syncJobArgs.dataLayout = SyncJobArgs.DataLayout.Payload

        return syncJobArgs
    }

    /**
     * wrap doBulkParallel and calls bulk
     *
     * @param dataList the list of data maps to create
     * @param jobContext the jobContext for the job
     * @return the id, just whats in jobContext
     */
    protected Long bulkExport(SyncJobContext jobContext) {

        //make sure it has a job here.
        Validate.notNull(jobContext.jobId)

        try {
            //jobContext.args.entityClass = getEntityClass()
            doBulkExport(jobContext)
        } catch (ex) {
            //ideally should not happen as the pattern here is that all exceptions should be handled in doBulkParallel
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
        finally {
            jobContext.finishJob()
        }

        return jobContext.jobId
    }

    protected GormRepo<D> getRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    /**
     * Run bulk export job
     */

    protected void doBulkExport(SyncJobContext jobContext) {
        try {
            //paginate and fetch data list, update job results for each page of data.
            eachPage(jobContext) { MetaMapList pageData ->
                Result result = Result.OK().payload(pageData as List)
                //update job with page data
                jobContext.updateJobResults(result, false, pageData.size())
            }
        } catch (Exception ex) {
            log.error("BulkExport unexpected exception", ex)
            jobContext.updateWithResult(problemHandler.handleUnexpected(ex))
        }
    }

    /**
     * Instead of loading all the data for bulkexport, it paginates and loads one page at a time
     */
    protected void eachPage(SyncJobContext jobContext, Closure cl) {
        SyncJobArgs args = jobContext.args
        Pager parentPager = setupPager(jobContext)
        parentPager.eachPage { page, max, offset ->
            MetaMapList entityMapList = runPageQuery(args, Pager.of(max: max, page: page))
            cl.call(entityMapList)
        }
    }

    /**
     * setup pager and do args.saveDataAsFile
     */
    protected Pager setupPager(SyncJobContext jobContext) {
        Pager paginator = Pager.of(max:gormConfig.bulk.exportPageSize)
        //count total records based on query args and build a paginator
        paginator.recordCount = getTotalCount(jobContext.args.queryArgs)
        jobContext.payloadSize = paginator.recordCount
        //hack right here to set saveDataAsFile when over 1000
        // if(paginator.recordCount > 1000){
        //     jobContext.args.saveDataAsFile = true
        // }
        //So CSV will work we always just do saveDataAsFile=true
        jobContext.args.saveDataAsFile = true

        return paginator
    }

    /**
     * run and call closure in Transaction.
     *
     */
    @ReadOnly
    protected MetaMapList runPageQuery(SyncJobArgs args, Pager pager) {
        List listPage = getRepo().query(args.queryArgs).pagedList(pager)
        //create metamap list with includes
        MetaMapList entityMapList = metaMapService.createMetaMapList(listPage, args.includes)
        //hydrate it now in transaction so we dont later get the "could not initialize proxy - no Session" when converting to json
        entityMapList.hydrate()
        getRepo().clear()
        return entityMapList

    }

    @ReadOnly
    protected Integer getTotalCount(QueryArgs queryArgs) {
        return getRepo().query(queryArgs).count() as Integer
    }

}
