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

import gorm.tools.repository.PersistArgs
import gorm.tools.repository.model.DataOp

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@CompileStatic
class SyncJobArgs {

    SyncJobArgs() { this([:])}

    SyncJobService syncJobService //reference to the syncJobService

    String source

    String sourceId

    /**
     * Payload input data used for job operations
     */
    Object payload

    /**
     * force how to store the payload
     */
    Boolean savePayload = true

    /**
     * force payload to store as file instead of bytes
     */
    Boolean savePayloadAsFile = false

    /**
     * resulting data is always saved but can force it to save to file instead of bytes in column
     */
    Boolean saveDataAsFile = false

    /**
     * the operation to perform, Used in bulk and limited to add and update right now.
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
    int errorThreshold = 0

    /**
     * if true then the bulk operation is all or nothing, meaning 1 error and it will roll back.
     * TODO not implemented yet
     */
    boolean transactional = false

    /**
     * Normally used for testing and debugging, or when encountering deadlocks.
     * Allows to override and turn off the AsyncArgs.enabled passed to ParallelTools
     * When the processes slices it will parallelize and run them async. If false then will not run in parallel and will be single threaded
     */
    Boolean parallel

    /**
     * Whether it should run in async background thread and return the job immediately.
     * Essentially makes the job a sort of Promise or Future.
     * when false (default) run in a standard blocking synchronous thread and return when job is done
     */
    Boolean promiseEnabled = false

    /**
     * the args, such as flush:true etc.., to pass down to the repo methods
     */
    Map persistArgs

    /** returns new PersistArgs instance on each call */
    PersistArgs getPersistArgs() { return this.persistArgs ? PersistArgs.of(this.persistArgs) : new PersistArgs() }

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
    SyncJobContext context

    /** helper to return true if op=DataOp.add */
    boolean isCreate(){
        op == DataOp.add
    }

    static SyncJobArgs of(DataOp dataOp){
        new SyncJobArgs(op: dataOp)
    }

    static SyncJobArgs create(Map args = [:]){
        args.op = DataOp.add
        new SyncJobArgs(args)
    }

    static SyncJobArgs update(Map args = [:]){
        args.op = DataOp.update
        new SyncJobArgs(args)
    }


}
