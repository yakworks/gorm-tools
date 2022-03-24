/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import java.nio.file.Path
import java.text.DecimalFormat
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
import yakworks.commons.json.JsonStreaming
import yakworks.commons.lang.Validate
import yakworks.i18n.MsgService
import yakworks.problem.ProblemTrait

@SuppressWarnings('Println')
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

    int payloadSize

    AtomicInteger processedCount = new AtomicInteger()
    AtomicInteger problemCount = new AtomicInteger()

    Path dataPath

    /**
     * The job id, will get populated once the job is created
     */
    Long jobId

    Closure transformResultsClosure

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

        if(payload instanceof Collection && payload.size() > 1000) {
            args.savePayloadAsFile = true
            args.saveDataAsFile = true
        }

        if(args.savePayload){
            if (payload && args.savePayloadAsFile) {
                data.payloadId = writePayloadFile(payload as Collection<Map>)
            }
            else {
                String res = JsonEngine.toJson(payload)
                data.payloadBytes = res.bytes
            }
        }

        //the call to this createJob method is already wrapped in a new trx
        def jobEntity = syncJobService.repo.create(data, [flush: true, bindId: true]) as SyncJobEntity

        //inititialize the ApiResults to be used in process
        results = ApiResults.create()

        return this
    }

    void setPayloadSize(Object payload){
        if(payload instanceof Collection){
            this.payloadSize = payload.size()
        }
    }

    /**
     * Update the job resiults with the current progress info
     *
     * @param currentResults the ApiResults
     * @param startTimeMillis the start time in millis, used to deduce time elapsed
     */
    void updateJobResults(ApiResults currentResults,   Long startTimeMillis) {
        if(!currentResults.ok) ok.set(false)
        boolean curOk = ok.get()

        int processedSize = processedCount.addAndGet(currentResults.size())
        DecimalFormat decFmt = new DecimalFormat("0.0")
        BigDecimal endTime = (System.currentTimeMillis() - startTimeMillis) / 1000
        String timing = "${decFmt.format(endTime)}s"

        String mem = decFmt.format(getUsedMem())

        String message = "slice ok: ${currentResults.ok}, processed ${processedSize} of ${payloadSize} in ${timing}, used mem: ${mem}gb"
        if(!currentResults.ok){
            int problemSize = problemCount.addAndGet(currentResults.getProblems().size())
            message = "$message\n Has ${problemSize} problems so far"
        }
        if(log.isDebugEnabled()){
            println(message)
        }

        updateJob(currentResults, [id: jobId, ok: curOk, message: message])
    }

    void appendDataResults(ApiResults currentResults){
        if(args.saveDataAsFile){
            boolean isFirstWrite = false
            if(!dataPath) {
                isFirstWrite = true // its first time writing
                initJsonDataFile()
            }
            def writer = dataPath.newWriter(true)
            def sjb = new StreamingJsonBuilder(writer, JsonEngine.generator)
            def dataList = transformResults(currentResults)
            dataList.each {
                //if its not the first time writing out then add comma for last object
                if(!isFirstWrite) writer.write(',\n')
                sjb.call it
                if(isFirstWrite) isFirstWrite = false //set to false once 1st recod is written
            }
            IOUtils.flushAndClose(writer)
        } else {
            results.merge currentResults
        }

    }

    SyncJobEntity finishJob(List<Map> renderResults = [], List<Map> renderErrorResults = []) {
        Map data = [id: jobId, state: SyncJobState.Finished] as Map<String, Object>
        // XXX how do I insert these renderErrorResults into data.errorBytes ?
        if(renderErrorResults){
            //it fails, they are still ProblemTraits
            data.errorBytes = JsonEngine.toJson(renderErrorResults).bytes
        }
        if(args.saveDataAsFile){
            //close out the file
            dataPath.withWriterAppend { wr ->
                wr.write('\n]\n')
            }
            data['dataId'] = syncJobService.createAttachment(dataPath, "SyncJobData_${jobId}_.json")
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
        synchronized ("SyncJob${jobId}".toString().intern()) {
            syncJobService.updateJob(data)
            // append json to dataFile
            appendDataResults(currentResults)
        }
    }

    void initJsonDataFile() {
        String filename = "SyncJobData_${jobId}_.json"
        dataPath = syncJobService.createTempFile(filename)
        //init with the opening brace
        dataPath.withWriter { wr ->
            wr.write('[\n')
        }
    }

    List<Map> transformResults(ApiResults apiResults) {
        // exit fast if closure is used
        if(transformResultsClosure) {
            return transformResultsClosure.call(apiResults) as List<Map>
        }
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

    Long writePayloadFile(Collection<Map> payload){
        String filename = "SyncJobPayload_${jobId}_.json"
        Path path = syncJobService.createTempFile(filename)
        JsonStreaming.streamToFile(payload, path)
        return syncJobService.createAttachment(path, filename)
    }

    static BigDecimal getUsedMem(){
        int gb = 1024*1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / gb
    }

}
