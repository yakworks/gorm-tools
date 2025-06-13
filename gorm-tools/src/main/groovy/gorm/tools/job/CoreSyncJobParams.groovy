/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import yakworks.commons.map.Maps
import yakworks.etl.DataMimeTypes
import yakworks.meta.MetaUtils

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is used for Bulk operations.
 * Created at the start of the process, in controller this is created from the params passed the action
 * See BulkableRepo for its primary usage.
 */
@CompileStatic
class CoreSyncJobParams {

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
    List<String> includes // = ['id']
    //String includes = 'id'

    String includesKey // = ['id']

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
     * The full query args/params map that were passed into the call.
     * Can be used to get any extra custom items that were pased in, such as we do with "apply" when calling arAdjust bulk.
     */
    Map<String, Object> queryParams

    /**
     * asMap used to store the params in SyncJob table as well as for converting to SyncJobArgs
     */
    Map<String, Object> asMap(){
        Map<String, Object> mapVals = Maps.prune(MetaUtils.getProperties(this))
        //dont include the full queryParams key
        mapVals.remove('queryParams')
        //get any extra queryParams that are not already keys in this object
        // queryParams is a full map copy so remove the keys so we can merge whats left
        Map extraParams = Maps.omit(getQueryParams(), mapVals.keySet())
        //if there are any entries left then add them
        if(extraParams) mapVals.putAll(extraParams)
        mapVals
    }

    /**
     *  converts to data for queueing up (saving/creating) a SyncJob
     */
    Map<String, Object> asJobData(){
        //make sure to use getters so overrides in super works
        return [
            source: getSource(),
            sourceId: getSourceId(),
            params: asMap(),
            jobType: getJobType(),
            dataFormat: getDataFormat()
        ] as Map<String,Object>
    }
}
