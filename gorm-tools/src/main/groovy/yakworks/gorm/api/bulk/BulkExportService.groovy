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
import yakworks.meta.MetaMapList

@CompileStatic
@Slf4j
class BulkExportService<D> {

    @Autowired SyncJobService syncJobService
    @Autowired MetaMapService metaMapService
    @Autowired ProblemHandler problemHandler
    @Autowired IncludesConfig includesConfig
    @Autowired TrxService trxService

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
     * Creates a bulk export job and puts in hazel queue
     */
    SyncJobEntity queueJob(BulkExportJobParams jobParams) {
        jobParams.entityClassName = getEntityClass().name
        SyncJobArgs args = setupSyncJobArgs(jobParams)
        return syncJobService.queueJob(args)
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

        //make sure they passed in q or qsearch. queryArgs will have been populated if they did
        if(!syncJobArgs.queryArgs) {
            throw DataProblem.of('error.query.qRequired').detail("q criteria required").toException()
        }
        //do includes, one of the keys is required. This is not used on queue, only for run.
        if(!jobParams.includes && !jobParams.includesKey) {
            throw DataProblem.ex("includes or includesKey are required params")
        }
        //parse the params into the IncludesProps
        var incProps = new IncludesProps(
            includes: jobParams.includes, includesKey: jobParams.includesKey
        )
        //returns includes if thats passed in or looks up includeKey
        syncJobArgs.includes = includesConfig.findIncludes(getEntityClass(), incProps)

        //give it the bulkImport type
        syncJobArgs.jobType = 'bulk.export'

        syncJobArgs.sourceId = jobParams.sourceId

        syncJobArgs.entityClass = entityClass

        syncJobArgs.dataFormat = SyncJobArgs.DataFormat.Payload

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
                //XXX @SUD we need to support the DataMimeTypes.csv too.
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
    //XXX @SUD add tests for these, not reason not to be adding unit tests
    protected void eachPage(SyncJobContext jobContext, Closure cl) {
        SyncJobArgs args = jobContext.args
        Pager parentPager = setupPager(jobContext)
        QueryArgs queryArgs = args.queryArgs
        parentPager.eachPage { page, max, offset ->
            MetaMapList entityMapList = runPageQuery(args, Pager.of(max: max, page: page))
            cl.call(entityMapList)
        }
    }

    /**
     * setup pager and do args.saveDataAsFile
     */
    //XXX @SUD add tests
    protected Pager setupPager(SyncJobContext jobContext) {
        Pager paginator = Pager.of(max:500) //XXX @SUD why 10? changed to 100, lets make it configurable
        //count total records based on query args and build a paginator
        paginator.recordCount = getTotalCount(jobContext.args.queryArgs)
        jobContext.payloadSize = paginator.recordCount
        //hack right here to set saveDataAsFile when over 1000
        if(paginator.recordCount > 1000){
            jobContext.args.saveDataAsFile = true
        }

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
