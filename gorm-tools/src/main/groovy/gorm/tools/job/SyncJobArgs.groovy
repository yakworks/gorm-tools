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
import yakworks.etl.DataMimeTypes
import yakworks.json.groovy.JsonEngine

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for SyncJob operations in the context.
 * Created at the start of the process, in controller this is created from the params passed the action
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor(includeSuperProperties = true)
@ToString(includeSuperProperties =true, includeNames = true, includes = ['jobId', 'jobType', 'op', 'source', 'sourceId'])
@CompileStatic
class SyncJobArgs extends SyncJobParams {

    SyncJobArgs() { this([:])}

    /**
     * When params include a mango query this is the QueryArgs that are created from it. Used for the ExportSyncArgs.
     * Used for filtering in various jobs, BulkExport in gorm-tools for example usage
     */
    QueryArgs queryArgs

    //************* Override *************

    /**
     * If dataLayout=Payload then data is just a json list or map, and errors will be in the problems field. Bulk uses this way.
     * If dataLayout=Result then errors are mixed in and the syncJob.data is just a rendered list of the Result or Problem objects.
     * When dataLayout=Payload then the rendering of the data is only list of whats in each results payload.
     * as opposed to a list of Results objects when dataLayout=Result
     * For example if processing import then instead of getting syncJob.data as a list of results objects it will be a list of what
     * the requested export is, such as Invoices. would look as if the call was made to the rest endpoint for a list synchronously
     * Since data can only support a list of entities then any issues or errors get stored in the separate problems field,
     * syncjob.problems will be populated with error results
     */
    DataLayout dataLayout = DataLayout.Result

    @CompileStatic
    static enum DataLayout { Result, Payload }

    /**
     * Payload input data used for job operations
     */
    Object payload

    /**
     * if attachment already created, this is the attachmentId
     */
    Long payloadId

    /**
     * the operation to perform, Used in bulk and limited to add, update and upsert right now.
     */
    DataOp op

    /**
     * the args, such as flush:true etc.., to pass down to the repo methods
     * Helpful for bindId when bulk importing rows that have id already.
     */
    Map persistArgs

    /**
     * returns new PersistArgs instance on each call.
     * GormRepo make changes as it goes so we dont want the same one going through multiple cylces
     */
    PersistArgs getPersistArgs() {
        return this.persistArgs ? PersistArgs.of(this.persistArgs) : new PersistArgs()
    }

    /**
     * The job id, will get populated when the job is created, normally from the syncJobContext.
     */
    Long jobId

    /**
     * The domain on which the bulk is being performed
     * Used by event listeners to filter and process selectively
     */
    Class entityClass

    //reference back to the SyncJobContext built from these args.
    //SyncJobContext context

    /** helper to return true if op=DataOp.add */
    boolean isCreate(){
        op == DataOp.add
    }

    @Override
    // Boolean isSavePayloadAsFile(){
    //     //if its set then use it
    //     if(super.savePayloadAsFile != null) return super.savePayloadAsFile
    //     // When collection then check size and set args
    //     return (payload instanceof Collection && ((Collection)payload).size() > 1000)
    // }

    @Override
    Boolean isSaveDataAsFile(){
        //if its set then use it
        if(this.saveDataAsFile != null) return this.saveDataAsFile
        // Base it on the payload, if its big then assume data will be too.
        return (payload instanceof Collection && ((Collection)payload).size() > 1000)
    }

    static SyncJobArgs of(DataOp dataOp){
        new SyncJobArgs(op: dataOp)
    }

    static SyncJobArgs create(Map args = [:]){
        args.op = DataOp.add
        new SyncJobArgs(args)
    }

    // static SyncJobArgs update(Map args = [:]){
    //     args.op = DataOp.update
    //     new SyncJobArgs(args)
    // }

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
        if(params.op != null) syncJobArgs.op = EnumUtils.getEnumIgnoreCase(DataOp, params.op as String)
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
    @Override
    Map<String, Object> asJobData(){
        //make sure to use getters so overrides in super works
        var dta = [
            source: getSource(),
            sourceId: getSourceId(),
            params: getQueryParams(),
            jobType: getJobType(),
            dataFormat: getDataFormat()
        ] as Map<String,Object>
        //if its has id then pass it
        if(getJobId()) dta['id'] = getJobId()
        //convet payload if its set
        if(getPayload()){
            dta.payloadBytes = JsonEngine.toJson(getPayload()).bytes
        }
        return dta
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
