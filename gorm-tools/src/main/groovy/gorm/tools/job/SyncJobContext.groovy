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

import gorm.tools.async.AsyncArgs
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.utils.BenchmarkHelper
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.api.ResultUtils
import yakworks.api.problem.Problem
import yakworks.commons.io.IOUtils
import yakworks.commons.lang.Validate
import yakworks.json.groovy.JsonEngine
import yakworks.json.groovy.JsonStreaming
import yakworks.message.spi.MsgService
import yakworks.spring.AppCtx

/**
 * Holds the basic state and primary action methods for a Bulk job.
 * Creates and updates the job status as it progresses and finalizes its results when finished.
 */
@SuppressWarnings('Println')
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString
@Slf4j
@CompileStatic
class SyncJobContext {
    /** Thread safe state tracking, any problem will update this to false. */
    AtomicBoolean ok = new AtomicBoolean(true)

    //reference back to the syncJobService that created this.
    SyncJobService syncJobService

    /** Arguments for this job.*/
    SyncJobArgs args

    /** Tracks the start time of the job to use in logging updates*/
    Long startTime

    /** The master results object */
    ApiResults results

    /** Payload input data used for job operations */
    Object payload

    int payloadSize

    AtomicInteger processedCount = new AtomicInteger()
    AtomicInteger problemCount = new AtomicInteger()

    Path dataPath

    Closure transformResultsClosure

    SyncJobContext() { this([:])}

    /** creates a context from the SynJobArgs and assign a back reference to this in SyncJobArgs. */
    static SyncJobContext of(SyncJobArgs args){
        def sjc = new SyncJobContext(args: args)
        args.context = sjc
        return sjc
    }

    /** gets the jobId, stored in args. The job id gets populated once the job is created */
    Long getJobId(){ return args.jobId }

    /** create a job using the syncJobService.repo.create */
    SyncJobContext createJob(){
        Validate.notNull(payload)
        //get jobId early so it can be used, might not need this anymore
        args.jobId = ((IdGeneratorRepo)syncJobService.repo).generateId()
        setPayloadSize(payload)

        Map data = [
            id: args.jobId, source: args.source, sourceId: args.sourceId,
            state: SyncJobState.Running, payload: payload
        ] as Map<String,Object>

        if(payload instanceof Collection && payload.size() > 1000) {
            args.savePayloadAsFile = true
            args.saveDataAsFile = true
        }

        if(args.savePayload){
            if (payload && args.savePayloadAsFile) {
                data.payloadId = writePayloadFile(payload as Collection)
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

    /**
     * Update the job results with the current progress info
     *
     * @param apiResults the ApiResults
     * @param startTimeMillis the start time in millis, used to deduce time elapsed
     * @param throwEx if false then only does log.error and will not throw on an exception so flow is not disrupted if this flakes out
     */
    void updateJobResults(Result apiResults, boolean throwEx = true) {
        try {

            if(!apiResults.ok) {
                ok.set(false)
                //if not ok then update the problemCount
                int probCnt = (apiResults instanceof ApiResults) ? apiResults.getProblems().size() : 1
                problemCount.addAndGet(probCnt)
            }
            //increment the processedCount
            int processedCnt = (apiResults instanceof ApiResults) ? apiResults.list.size() : 1
            processedCount.addAndGet(processedCnt)

            String message = getJobUpdateMessage(apiResults.ok)

            updateJob(apiResults, [id: jobId, ok: ok.get(), message: message])
        } catch (e) {
            //ok to swallow this excep since we dont want to disrupt the flow
            log.error("Unexpected error during updateJobResults", e)
            if(throwEx) throw e
        }
    }

    /**
     * updates job with a result or a problem
     */
    void updateWithResult(Result result) {
        if(!result.ok) {
            ok.set(false)
            problemCount.addAndGet(1)
        }
        updateJob(result, [id: jobId, ok: ok.get()])
    }

    /**
     * Finalize Job by completing data bytes and setting state to Finished.
     * @return the finalized SyncJobEntity
     */
    SyncJobEntity finishJob() {
        Map data = [id: jobId, state: SyncJobState.Finished] as Map<String, Object>
        if(args.saveDataAsFile){
            // if saveDataAsFile then it will have been writing out the data results as it goes
            //close out the file
            dataPath.withWriterAppend { wr ->
                wr.write('\n]\n')
            }
            data['dataId'] = syncJobService.createAttachment(dataPath, "SyncJobData_${jobId}_.json")
        } else {
            // if NOT saveDataAsFile then we need to write out the results to dataBytes since results have not been written out yet.
            List<Map> renderResults = transformResults(results)

            if(args.saveErrorsSeparate) {
                //success results may not have ok:true at all in some cases - eg glExport scanerio, but failed always have ok:false
                List<Map> errors = renderResults.findAll { it.containsKey('ok') && it.ok == false}
                data.errorBytes =  JsonEngine.toJson(errors).bytes
                renderResults = renderResults - errors
            }

            data.dataBytes = JsonEngine.toJson(renderResults).bytes
            data.ok = ok.get()
        }
        SyncJobEntity entity = syncJobService.updateJob(data)
        AppCtx.publishEvent(SyncJobFinishedEvent.of(this))
        return entity
    }

    /**
     * Standard transformation for the apiResults into a List of Maps so it can be saved as databytes
     * @param resultsToTransform normally an ApiResults but can be any Problem or Result
     * @return the transformed List
     */
    List<Map> transformResults(Result resultToTransform) {
        // exit fast if closure is used
        if(transformResultsClosure) {
            return transformResultsClosure.call(resultToTransform) as List<Map>
        }

        List<Result> resultList = (resultToTransform instanceof ApiResults) ? resultToTransform.list : [ resultToTransform ]
        MsgService msgService = syncJobService.messageSource
        List<Map> ret = []
        boolean ok = true
        for (Result r : resultList) {
            def map = [ok: r.ok, status: r.status.code, data: r.payload] as Map<String, Object>
            //do the failed
            if (r instanceof Problem) {
                map.putAll([
                    code: r.code,
                    title: ResultUtils.getMessage(msgService, r),
                    detail: r.detail,
                ])
                if(r.violations) map["errors"] = r.violations //put errors only if violations are not empty
            }
            // else {
            //     map.data = r.payload as Map
            // }
            ret << map
        }
        return ret
    }

    /**
     * builds a status message to update the job
     */
    protected String getJobUpdateMessage(boolean resOk){
        String timing = startTime ? " | ${BenchmarkHelper.elapsedTime(startTime)}" : ''
        String mem = " | used mem: ${BenchmarkHelper.getUsedMem()}"

        String message = "slice ok: ${resOk} | processed ${processedCount.get()} of ${payloadSize}${timing}${mem}"
        int problemSize = problemCount.get()
        if(problemSize){
            message = "$message | Has ${problemSize} problems so far"
        }
        if(log.isDebugEnabled()){
            log.debug(message)
        }
        return message
    }

    /**
     * called from createJob to set the payloads size. which is used to decide whether its stored at file or in db as bytes.
     */
    protected void setPayloadSize(Object payload){
        if(payload instanceof Collection){
            this.payloadSize = payload.size()
        }
    }

    /**
     * Update the job with status on whats been processed and append the json data
     * @param currentResults the results to append, normally will be an ApiResults but can be any Problem or Result
     */
    protected void updateJob(Result currentResults, Map data){
        //sync to only one thread for the SyncJob can update at a time
        synchronized ("SyncJob${jobId}".toString().intern()) {
            syncJobService.updateJob(data)
            // append json to dataFile
            appendDataResults(currentResults)
        }
    }

    /**
     * Append the results. called from updateJob
     * @param currentResults the results to append, normally will be an ApiResults but can be any Problem or Result
     */
    protected void appendDataResults(Result currentResults){
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
                isFirstWrite = false  //set to false once 1st recod is written with a comma ","
            }
            IOUtils.flushAndClose(writer)
        } else {
            results.merge currentResults
        }

    }

    /**
     * when args.saveDataAsFile = true, this is called to initialize the datafile
     */
    protected void initJsonDataFile() {
        String filename = "SyncJobData_${jobId}_.json"
        dataPath = syncJobService.createTempFile(filename)
        //init with the opening brace
        dataPath.withWriter { wr ->
            wr.write('[\n')
        }
    }

    /**
     * when args.savePayload and args.savePayloadAsFile are true, this is called to save the payload to file
     * @param payload the payload List or Map that was sent (will normally have been json when called via REST
     * @return the Attachment id.
     */
    protected Long writePayloadFile(Collection payload){
        String filename = "SyncJobPayload_${jobId}_.json"
        Path path = syncJobService.createTempFile(filename)
        JsonStreaming.streamToFile(payload, path)
        return syncJobService.createAttachment(path, filename)
    }

    /**
     * helper to add memory usage to the progress message.
     * @return the used mem in gigabytes.
     */
    static BigDecimal getUsedMem(){
        int gb = 1024*1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime()
        return (runtime.totalMemory() - runtime.freeMemory()) / gb
    }

}
