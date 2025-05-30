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

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString(includeNames = true, includes = ['jobId', 'op', 'source', 'sourceId', 'async'])
@CompileStatic
class SyncJobArgs {

    SyncJobArgs() { this([:])}

    String source

    String sourceId

    /** the type of the SyncJob, used for queue and to switch on routing to run */
    String jobType

    /**
     * Payload input data used for job operations
     */
    Object payload

    /**
     * if attachment already created, this is the attachmentId
     */
    Long payloadId

    /**
     * force payload to store as file instead of bytes
     */
    Boolean savePayloadAsFile

    /**
     * resulting data (what is returned in response) is always saved but can force it to save to file instead of bytes in column
     */
    Boolean saveDataAsFile = false

    /**
     * If dataFormat=Payload then data is just a json list or map, and errors will be in the problems field. Bulk uses this way.
     * If dataFormat=Result then errors are mixed in and the syncJob.data is just a rendered list of the Result or Problem objects.
     * When dataFormat=Payload then the rendering of the data is only list of whats in each results payload.
     * as opposed to a list of Results objects when dataFormat=Result
     * For example if processing export then instead of getting syncJob.data as a list of results objects it will be a list of what
     * the requested export is, such as Invoices. would look as if the call was made to the rest endpoint for a list synchronously
     * Since data can only support a list of entities then any issues or errors get stored in the separate problems field,
     * syncjob.errorBytes will be populated with error results
     */
    DataFormat dataFormat = DataFormat.Result

    @CompileStatic
    static enum DataFormat { Result, Payload }

    /**
     * the operation to perform, Used in bulk and limited to add, update and upsert right now.
     */
    DataOp op

    /**
     * extra params to pass into Job, such as source and sourceId. The endpoint the
     * request came from will end up in sourceId
     */
    Map params = [:]


    /**
     * for results, list of fields to include for the SyncJob.data
     */
    List<String> includes = ['id']

    /**
     * List of keys to include from the data map that failed.
     * default is null which means it returns the originalData that was submitted.
     */
    List<String> errorIncludes = null

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
     */
    Boolean async = true

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
     * When params include a mango query this is the QueryArgs that are created from it. Used for the ExportSyncArgs.
     * FUTURE USE
     */
    QueryArgs queryArgs

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

    boolean isSavePayloadAsFile(){
        //if its set then use it
        if(this.savePayloadAsFile != null) return this.savePayloadAsFile
        // When collection then check size and set args
        return (payload instanceof Collection && ((Collection)payload).size() > 1000)

    }

    boolean isSaveDataAsFile(){
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
        SyncJobArgs syncJobArgs = new SyncJobArgs(params: params)
        //parallel is NULL by default
        if(params.parallel != null) syncJobArgs.parallel = params.getBoolean('parallel')

        //when this is true then runs "non-blocking" in background and will job immediately with state=running
        syncJobArgs.async = params.getBoolean('async', true)

        if(params.saveDataAsFile != null) syncJobArgs.saveDataAsFile = params.getBoolean('saveDataAsFile')

        syncJobArgs.sourceId = params.sourceId
        //can use both jobSource and source to support backward compat, jobSource wins if both are set
        if(params.source != null) syncJobArgs.source = params.source
        if(params.jobSource != null) syncJobArgs.source = params.jobSource
        if(params.jobType != null) syncJobArgs.jobType = params.jobType

        //allow to specify the dataFormat
        if(params.dataFormat != null) syncJobArgs.dataFormat = EnumUtils.getEnumIgnoreCase(DataFormat, params.dataFormat as String)

        //setup queryArgs
        if(params.containsKey("q") || params.containsKey("qSearch") ) {
            syncJobArgs.queryArgs = QueryArgs.of(params)
        }

        return syncJobArgs
    }

    AsyncArgs getAsyncArgs() {
        return new AsyncArgs(enabled: async)
    }

}
