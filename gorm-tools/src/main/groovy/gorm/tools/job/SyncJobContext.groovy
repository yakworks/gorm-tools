/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.MapConstructor
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.util.logging.Slf4j

import gorm.tools.repository.model.IdGeneratorRepo
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.api.ResultUtils
import yakworks.commons.io.IOUtils
import yakworks.commons.json.JsonEngine
import yakworks.commons.lang.Validate
import yakworks.i18n.MsgService
import yakworks.problem.ProblemTrait

@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@Slf4j
@CompileStatic
class SyncJobContext {

    AtomicBoolean ok = new AtomicBoolean(true)

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

    Long payloadId

    int payloadSize

    AtomicInteger processedCount = new AtomicInteger()
    AtomicInteger problemCount = new AtomicInteger()

    Path dataPath

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
        //get jobId early so it can be used, might not need this anymore
        jobId = ((IdGeneratorRepo)syncJobService.repo).generateId()
        setPayloadSize(payload)

        Map data = [
            id: jobId, source: args.source, sourceId: args.sourceId,
            state: SyncJobState.Running, payload: payload
        ] as Map<String,Object>

        //the calle to this method is already wrapped in a new trx
        def jobEntity = syncJobService.repo.create(data, [flush: true, bindId: true]) as SyncJobEntity

        //if repo used payloadId file then will use file and dataId to stream results too
        payloadId = jobEntity.payloadId

        //inititialize the ApiResults to be used in process
        results = ApiResults.create()

        return this
    }

    void setPayloadSize(Object payload){
        if(payload instanceof Collection){
            this.payloadSize = payload.size()
        }
    }

    void updateJobResults(ApiResults currentResults) {
        if(!currentResults.ok) ok.set(false)
        boolean curOk = ok.get()

        int processedSize = processedCount.addAndGet(currentResults.size())
        String message = "current results ok:${currentResults.ok} Processed ${processedSize} of ${processedSize}"
        if(!currentResults.ok){
            int problemSize = problemCount.addAndGet(currentResults.getProblems().size())
            message = "$message, with ${problemSize} problems so far"
        }
        updateJob(currentResults, [id: jobId, ok: curOk, message: message])
    }

    void appendDataResults(ApiResults currentResults){
        if(payloadId){
            if(!dataPath) initJsonDataFile()
            def writer = dataPath.newWriter(true)
            def sjb = new StreamingJsonBuilder(writer, JsonEngine.generator)
            def dataList = transformResults(currentResults)
            sjb.call dataList
            writer.write(',\n')
            IOUtils.flushAndClose(writer)
        } else {
            results.merge currentResults
        }

    }

    SyncJobEntity finishJob(List<Map> renderResults = []) {
        Map data = [id: jobId, state: SyncJobState.Finished] as Map<String, Object>
        if(payloadId){
            //close out the file
            dataPath.withWriter { wr ->
                wr.write(']\n')
            }
        } else {
            renderResults = renderResults ?: transformResults(results)
            data.dataBytes = JsonEngine.toJson(renderResults).bytes
            data.ok = ok.get()
        }
        return syncJobService.updateJob(data)
    }

    /**
     * Update the job with status on whats been processed and append the json data
     */
    void updateJob(ApiResults currentResults, Map data){
        //sync to only one thread for the SyncJob can update at a time
        synchronized ("SyncJob${jobId}".intern()) {
            syncJobService.updateJob(data)
            // append json to dataFile
            appendDataResults(currentResults)
        }
    }

    void initJsonDataFile() {
        String filename = "SyncJobPData_${jobId}_.json"
        dataPath = syncJobService.createTempFile(filename)
        //init with the opening brace
        dataPath.withWriter { wr ->
            wr.write('[\n')
        }
    }

    List<Map> transformResults(ApiResults apiResults) {
        MsgService msgService = syncJobService.messageSource
        List<Map> ret = []
        boolean ok = true
        for (Result r : apiResults) {
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
