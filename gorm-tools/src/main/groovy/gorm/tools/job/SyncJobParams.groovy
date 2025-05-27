/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.mango.api.QueryArgs
import gorm.tools.repository.model.DataOp

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class SyncJobParams {

    /**
     * force how to store the payload (what was sent)
     */
    Boolean savePayload = true

    /**
     * force payload to store as file instead of bytes
     */
    Boolean savePayloadAsFile

    /**
     * resulting data (what is returned in response) is always saved but can force it to save to file instead of bytes in column
     */
    Boolean saveDataAsFile = false

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
     * When params include a mango query this is the QueryArgs that are created from it. Used for the ExportSyncArgs.
     * FUTURE USE
     */
    QueryArgs queryArgs

    /**
     * The domain on which the bulk is being performed
     * Used by event listeners to filter and process selectively
     */
    String entityClassName

}
