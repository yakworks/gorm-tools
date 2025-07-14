/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.async.AsyncArgs
import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.DataOp
import yakworks.commons.lang.EnumUtils
import yakworks.commons.map.Maps
import yakworks.etl.DataMimeTypes
import yakworks.json.groovy.JsonEngine
import yakworks.meta.MetaUtils

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for SyncJob operations in the context.
 * Created at the start of the process, in controller this is created from the params passed the action
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString(includeNames = true, includes = ['jobId', 'jobType', 'source', 'sourceId'])
@CompileStatic
class SyncJobArgs {

    SyncJobArgs() { this([:])}

    /**
     * The job id, will get populated when the job is created, normally from the syncJobContext.
     */
    Long jobId

    /**
     * Job Type key
     */
    String jobType

    /**
     * You can specify consistent source name for the data, for example “Oracle ERP”, “Dynamics” etc…
     */
    String source

    /**
     * Usually set automatically and does not need to be filled
     */
    String sourceId

    /**
     * Normally used for testing and debugging, or when encountering deadlocks.
     * Allows to override and turn off the AsyncArgs.enabled passed to ParallelTools
     * When the processes slices it will parallelize and run them async. If false then will not run in parallel and will be single threaded
     * NOTE: This is null by default and should not default to true/false, when it gets set to AsyncArgs.enabled if thats null
     * then it will use the system/config defaults from gorm.tools.async.enabled
     */
    Boolean parallel //keep NULL by default

    /**
     * Whether it should run in async background thread and return the job immediately.
     * Essentially makes the job a sort of Promise or Future.
     * when false (default) run in a standard blocking synchronous thread and return when job is done
     * NOTE: Some SyncJobs only run as async=true, BulkExport for example.
     */
    Boolean async

    /**
     * For results, comma seperated list of fields to include for the SyncJob.data
     */
    List<String> includes //= ['id']

    /**
     * can pass in the key to look up form our includes
     */
    String includesKey

    /**
     * List of keys to include from the data map that failed.
     * default is null which means it returns the originalData that was submitted.
     */
    List<String> errorIncludes //= null
    //String errorIncludes

    /**
     * some syncjobs can involve a "q" mango query
     */
    String q

    /**
     * force payload to store as file instead of bytes
     * Primarily used for dev/tests
     */
    Boolean savePayloadAsFile

    /**
     * resulting data (what is returned in response) is always saved but can force it to save to file instead of bytes in column
     * Primarily used for dev/tests
     */
    Boolean saveDataAsFile //= false

    /**
     * (When attachmentId is set) Format for the data. either CSV or JSON are currently supported.
     */
    DataMimeTypes dataFormat = DataMimeTypes.json

    /**
     * If dataLayout=Payload then data is just a json list or map as the data it, and errors will be in the problems field.
     * BulkExport uses Payload for example.
     * dataLayout=Payload IS the recomended way.
     * If dataLayout=Result then data is a list of Result/Problem objects.
     * Errors are mixed in and the syncJob.data is just a rendered list of the Result or Problem objects.
     * BulkImport uses Payload for example. This is considered deprecated.
     * When dataLayout=Payload then the rendering of the data is only list of whats in each results payload.
     * as opposed to a list of Results objects when dataLayout=Result
     * For example if processing import then instead of getting syncJob.data as a list of results objects it will be a list of what
     * the requested export is, such as Invoices. would look as if the call was made to the rest endpoint for a list synchronously
     * Since data can only support a list of entities then any issues or errors get stored in the separate problems field,
     * syncjob.problems will be populated with error results
     */
    DataLayout dataLayout = DataLayout.Result

    /**
     * Payload input data used for job operations
     */
    Object payload //transient in asMap

    /**
     * if attachment already created, this is the attachmentId
     */
    Long payloadId

    /**
     * The full query args/params map that were passed into the call.
     * Constructed and cached here in the withParams method
     * Can be used to get any extra custom items that were pased in, such as we do with "apply" when calling arAdjust bulk.
     */
    Map<String, Object> queryParams

    /**
     * When params include a mango query this is the QueryArgs that are created from it. Used for the ExportSyncArgs.
     * Used for filtering in various jobs, BulkExport in gorm-tools for example usage
     */
    QueryArgs queryArgs //transient in asMap

    /**
     * the operation to perform, Used in bulk and limited to add, update and upsert right now.
     */
    // DataOp op

    /**
     * the args, such as flush:true etc.., to pass down to the repo methods
     * Helpful for bindId when bulk importing rows that have id already.
     */
    //PersistArgs persistArgs //= PersistArgs.defaults()

    /**
     * The domain on which the bulk is being performed
     * Used by event listeners to filter and process selectively
     * FIXME only needed for BulkImport legacy to set calss on the BulkImportFinishedEvent, once we are doing new way we can remove this
     */
    // Class entityClass

    // Boolean isSavePayloadAsFile(){
    //     //if its set then use it
    //     if(super.savePayloadAsFile != null) return super.savePayloadAsFile
    //     // When collection then check size and set args
    //     return (payload instanceof Collection && ((Collection)payload).size() > 1000)
    // }

    Boolean shouldSaveDataAsFile(){
        //if its set then use it
        if(this.saveDataAsFile != null) return this.saveDataAsFile
        // Base it on the payload, if its big then assume data will be too.
        return (payload instanceof Collection && ((Collection)payload).size() > 1000)
    }

    static SyncJobArgs withParams(Map params){
        SyncJobArgs syncJobArgs = new SyncJobArgs(queryParams: params)
        //parallel is NULL by default
        if(params.parallel != null) syncJobArgs.parallel = params.getBoolean('parallel')

        //when this is true then runs "non-blocking" in background and will job immediately with state=running
        syncJobArgs.async = params.getBoolean('async', true)

        if(params.saveDataAsFile != null) syncJobArgs.saveDataAsFile = params.getBoolean('saveDataAsFile')

        syncJobArgs.sourceId = params.sourceId
        //can use both jobSource and source to support backward compat, jobSource wins if both are set

        if(params.source != null) syncJobArgs.source = params.source
        //Support legacy param if they pass jobSource it will win
        if(params.jobSource != null) syncJobArgs.source = params.jobSource
        if(params.jobType != null) syncJobArgs.jobType = params.jobType
        //if(params.op != null) syncJobArgs.op = EnumUtils.getEnumIgnoreCase(DataOp, params.op as String)
        if(params.dataFormat != null) syncJobArgs.dataFormat = EnumUtils.getEnumIgnoreCase(DataMimeTypes, params.dataFormat as String)

        //allow to specify the dataLayout
        if(params.dataLayout != null) syncJobArgs.dataLayout = EnumUtils.getEnumIgnoreCase(DataLayout, params.dataLayout as String)

        //setup queryArgs
        if(params.containsKey("q") || params.containsKey("qSearch") ) {
            syncJobArgs.queryArgs = QueryArgs.of(params)
        }

        return syncJobArgs
    }

    AsyncArgs getAsyncArgs() {
        return new AsyncArgs(enabled: async)
    }

    /**
     *  converts to data for queueing up (saving/creating) a SyncJob
     *  Can probably get rid of this, used mostly for the old way of doing it with createJob.
     */
    // Map<String, Object> asJobData(){
    //     //make sure to use getters so overrides in super works
    //     var dta = [
    //         source: getSource(),
    //         sourceId: getSourceId(),
    //         //params: getQueryParams(),
    //         params: asMap(),
    //         jobType: getJobType(),
    //         dataFormat: getDataFormat(),
    //         dataLayout: getDataLayout()
    //     ] as Map<String,Object>
    //     //if its has id then pass it
    //     if(getJobId()) dta['id'] = getJobId()
    //     //convet payload if its set
    //     if(getPayload()){
    //         dta.payloadBytes = JsonEngine.toJson(getPayload()).bytes
    //     }
    //     return dta
    // }

    /**
     * asMap used to store the params in SyncJob table
     */
    Map<String, Object> asMap(){
        //excludes
        // entityClass, asyncArgs, queryArgs
        Map<String, Object> mapVals = Maps.prune(MetaUtils.getProperties(this))
        //dont include the full queryParams key
        ['jobId', 'entityClass', 'asyncArgs', 'queryArgs', 'queryParams', 'payload'].each {
            mapVals.remove(it)
        }
        //get any extra queryParams that are not already keys in this object
        // queryParams is a full map copy so remove the keys so we can merge whats left
        Map extraParams = Maps.omit(getQueryParams(), mapVals.keySet())
        //if there are any entries left then add them
        if(extraParams) mapVals.putAll(extraParams)
        mapVals
    }

    /**
     * percentage of errors before it stops the job.
     * for example, if 1000 records are passed and this is set to default 10 then
     * the job will halt when it hits 100 errors
     * this setting ignored if transactional=true
     */
    //TODO not implemented yet
    // int errorThreshold = 0

    /**
     * if true then the bulk operation is all or nothing, meaning 1 error and it will roll back.
     * TODO not implemented yet
     */
    // boolean transactional = false
}
