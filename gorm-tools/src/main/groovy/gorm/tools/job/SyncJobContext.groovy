/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path

import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.repository.model.IdGeneratorRepo
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.api.ResultUtils
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate
import yakworks.i18n.MsgService
import yakworks.problem.ProblemTrait

@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@CompileStatic
class SyncJobContext {

    SyncJobService syncJobService //reference to the syncJobService

    SyncJobArgs args

    /**
     * The master results object
     */
    ApiResults results

    /**
     * Payload input data used for job operations
     */
    Object payload

    /**
     * The job id, will get populated once the job is created
     */
    Long jobId

    SyncJobContext() { this([:])}

    static SyncJobContext create(Map params = [:]){
        def sjc = new SyncJobContext(params)
        return sjc
    }

    SyncJobContext createJob(){
        Validate.notNull(payload)
        jobId = ((IdGeneratorRepo)syncJobService.repo).generateId()
        Map data = [id: jobId, source: args.source, sourceId: args.sourceId, state: SyncJobState.Running] as Map<String,Object>

        if(args.payloadStorageType == SyncJobArgs.StorageType.BYTES) {
            data.payloadBytes = JsonEngine.toJson(payload).bytes
        }
        else if(args.payloadStorageType == SyncJobArgs.StorageType.FILE){
            data.payloadId = writePayloadFile()
        }

        def jobEntity = syncJobService.repo.create(data, [flush: true, bindId: true]) as SyncJobEntity

        //inititialize the ApiResults to be used in process
        results = ApiResults.create()

        return this
    }

    void updateJobResults(ApiResults currentResults) {
        //add summary to master
        // if(currentResults.ok){
        //     masterResults.add Result.OK().title("WTF")
        // } else {
        //
        // }
        results.merge currentResults
    }

    SyncJobEntity finishJob(List<Map> renderResults = []) {
        renderResults = renderResults ?: transformResults()
        byte[] dataBytes = JsonEngine.toJson(renderResults).bytes
        Map data = [id: jobId, ok: results.ok, dataBytes: dataBytes, state: SyncJobState.Finished]
        return syncJobService.repo.update(data, [flush: true]) as SyncJobEntity
    }

    Long writePayloadFile(){
        String filename = "SyncJobPayload_${jobId}_.json"
        Path path = syncJobService.createTempFile(filename)
        JsonEngine.streamToFile(path, payload)
        return syncJobService.createAttachment([name: filename, sourcePath: path])
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

    List<Map> transformResults() {
        MsgService msgService = syncJobService.messageSource
        List<Map> ret = []
        boolean ok = true
        for (Result r : results) {
            def map = [ok: r.ok, status: r.status.code, data: r.payload] as Map<String, Object>
            //do the failed
            if (r instanceof ProblemTrait) {
                map.putAll([
                    code: r.code,
                    title: ResultUtils.getMessage(msgService, r),
                    detail: r.detail,
                    errors: r.violations
                ])
            } else {
                map.data = r.payload as Map
            }
            ret << map
        }
        return ret
    }

}
