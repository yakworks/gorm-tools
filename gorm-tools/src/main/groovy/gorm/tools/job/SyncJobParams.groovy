/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import yakworks.commons.map.Maps
import yakworks.etl.DataMimeTypes
import yakworks.meta.MetaUtils

/**
 * Value Object are better than using a Map to store arguments and parameters.
 * This is the base class, see BulkImportJobArgs and BulkExportJobParams for implementaiton versions
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class SyncJobParams {

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
     * If dataLayout=List then data is just a json list or map as the data it, and errors will be in the problems field.
     * BulkExport uses List for example.
     * dataLayout=List IS the recomended way.
     * If dataLayout=Result then data is a list of Result/Problem objects.
     * Errors are mixed in and the syncJob.data is just a rendered list of the Result or Problem objects.
     * BulkImport uses Result for example. This is considered deprecated.
     * When dataLayout=List then the rendering of the data is only list of whats in each results payload.
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
    Object payload

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
