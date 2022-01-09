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

import gorm.tools.repository.GormRepo
import yakworks.api.ApiResults
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate

@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@CompileStatic
class SyncJobContext {

    SyncJobContext() { this([:])}

    GormRepo syncJobRepo //reference to the syncJobService

    SyncJobArgs args

    /**
     * Payload input data used for job operations
     */
    Object payload

    /**
     * The job id, will get populated once the job is created
     */
    Long jobId

    /**
     * returns map for used for creating SyncJobEntity
     */
    Map getJobData() {
        Validate.notNull(args.payload)
        byte[] reqData = JsonEngine.toJson(args.payload).bytes
        return [source: args.source, sourceId: args.sourceId, state: SyncJobState.Running, requestData: reqData]
    }

    SyncJobContext createJob(){
        Validate.notNull(payload)
        Map data = [source: args.source, sourceId: args.sourceId, state: SyncJobState.Running] as Map<String,Object>

        if(args.payloadStorageType == SyncJobArgs.StorageType.BYTES) {
            data.requestData = JsonEngine.toJson(payload).bytes
        }
        else if(args.payloadStorageType == SyncJobArgs.StorageType.FILE){
            //TODO save to file
            println "FIXME not implemented yet"
        }

        def jobEntity = syncJobRepo.create(data, [flush: true]) as SyncJobEntity
        jobId = jobEntity.id

        return this
    }

    SyncJobEntity updateJob(SyncJobState state, ApiResults results, List<Map> renderResults) {
        byte[] resultBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id: jobId, ok: results.ok, data: resultBytes, state: state]
        return syncJobRepo.update(data, [flush: true]) as SyncJobEntity
    }

    /**
     * Update the job with status on whats been processed and append the json data
     */
    // void updateJob(SyncJobArgs syncJobArgs, ApiResults apiResults){
    //     //sync to only one thread for the SyncJob can update at a time
    //     synchronized ("SyncJob${syncJobArgs.jobId}".intern()) {
    //
    //     }
    // }

}
