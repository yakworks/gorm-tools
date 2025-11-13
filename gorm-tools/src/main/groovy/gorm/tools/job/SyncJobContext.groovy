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
import groovy.transform.Synchronized
import groovy.transform.ToString
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.StackTraceUtils

import gorm.tools.job.events.SyncJobFinishedEvent
import gorm.tools.utils.BenchmarkHelper
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.api.ResultUtils
import yakworks.api.problem.Problem
import yakworks.commons.io.IOUtils
import yakworks.etl.CSVMapWriter
import yakworks.etl.DataMimeTypes
import yakworks.json.groovy.JsonEngine
import yakworks.message.spi.MsgService
import yakworks.spring.AppCtx

/**
 * Holds the basic state and primary action methods while running a SyncJoob.
 * Creates and updates the job status as it progresses and finalizes its results when finished.
 */
@SuppressWarnings('Println')
@Builder(builderStrategy= SimpleStrategy, prefix="")
@MapConstructor
@ToString(includeNames = true, includes = ['ok', 'startTime', 'jobId'])
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

    /** The problems will be populated if dataLayout="List or Map", then this will be where they are stored */
    List<Problem> problems = [] as List<Problem>

    /** Payload input data used for job operations */
    Object getPayload(){
        args.payload
    }

    /** Payload size used for progress messages*/
    int payloadSize

    AtomicInteger processedCount = new AtomicInteger()
    AtomicInteger problemCount = new AtomicInteger()

    Path dataPath

    Closure transformResultsClosure

    SyncJobContext() { this([:])}

    /** creates a context from the SynJobArgs and assign a back reference to this in SyncJobArgs. */
    static SyncJobContext of(SyncJobArgs args){
        def sjc = new SyncJobContext(args: args)
        //args.context = sjc
        return sjc
    }

    /**
     * gets the jobId, stored in args.
     * The job id gets populated once the job is created or startJob called
     */
    Long getJobId(){ return args.jobId }

    /**
     * Update the job results with the current progress info
     *
     * @param apiResults the ApiResults
     * @param startTimeMillis the start time in millis, used to deduce time elapsed
     * @param throwEx if false then only does log.error and will not throw on an exception so flow is not disrupted if this flakes out
     */
    void updateJobResults(Result apiResults, boolean throwEx = true, Integer sliceCount = null) {
        try {

            if(!apiResults.ok) {
                ok.set(false)
                //if not ok then update the problemCount
                int probCnt = (apiResults instanceof ApiResults) ? apiResults.getProblems().size() : 1
                problemCount.addAndGet(probCnt)
            }
            //increment the processedCount
            int processedCnt = (apiResults instanceof ApiResults) ? apiResults.list.size() : 1
            //if messageCoutn is passed in then use that for the messages
            if(sliceCount){
                processedCnt = sliceCount
            } else {
                processedCnt = (apiResults instanceof ApiResults) ? apiResults.list.size() : 1
            }
            processedCount.addAndGet(processedCnt)

            String message = getJobUpdateMessage(apiResults.ok)

            updateJob(apiResults, [id: jobId, ok: ok.get(), message: message])
        } catch (e) {
            //ok to swallow this excep since we dont want to disrupt the flow
            log.error("Unexpected error during updateJobResults", StackTraceUtils.deepSanitize(e))
            if(throwEx) throw e
        }
    }

    /**
     * Update the total counts for processedCount and problemCount
     * and updates message only with the counts, doesn't add or update any results.
     * FIXME WIP, needs tests, not used anywhere yet.
     *
     * @param processedCnt the count to add to the the total processedCount
     * @param probCnt the problems to add to the problemCount
     */
    void updateMessage(int processedCnt, int probCnt) {
        try {
            if(processedCnt) processedCount.addAndGet(probCnt)
            if(probCnt) problemCount.addAndGet(probCnt)

            String message = getJobUpdateMessage(probCnt > 0)

            updateJob(null, [id: jobId, ok: ok.get(), message: message])
        } catch (e) {
            //ok to swallow this excep since we dont want to disrupt the flow, really this should be async
            log.error("Unexpected error during updateJobResults", StackTraceUtils.deepSanitize(e))
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
        Map data = [id: jobId] as Map<String, Object>
        if(args.shouldSaveDataAsFile()){
            // if saveDataAsFile then it will have been writing out the data results as it goes
            //close out the file for JSON
            if(args.dataFormat == DataMimeTypes.json) {
                dataPath.withWriterAppend { wr ->
                    wr.write('\n]\n')
                }
            }
            String ext = args.dataFormat as String
            data['dataId'] = syncJobService.createAttachment(dataPath, "SyncJobData_${jobId}.${ext}")
        } else {
            // if NOT saveDataAsFile then we need to write out the results to dataBytes since results have not been written out yet.
            List<Map> renderResults = transformResults(results)
            data.dataBytes = JsonEngine.toJson(renderResults).bytes
        }

        //problems are always stored in problem field, regardless of data layout
        if(problems.size() > 0) {
            data.problems = problems*.asMap()
        }

        //first just update the data/databytes on syncjob to ensure that data is always available if syncjob = finished
        syncJobService.updateJob(data)

        //update "ok" and status after data is updated
        SyncJobEntity entity = syncJobService.updateJob([id:jobId, ok:ok.get(), state: SyncJobState.Finished])

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
        List<Map> ret = args.dataLayout == DataLayout.List ? transformResultPayloads(resultList) : transformResultToMap(resultList)
        return ret
    }

    List<Map> transformResultToMap(List<Result> resultList) {
        MsgService msgService = syncJobService.messageSource
        List<Map> resMapList = []
        for (Result r : resultList) {
            //always add problems too
            if (r instanceof Problem) {
                problems.add(r)
            }
            //these are common across both problems and success results.
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
            resMapList <<  map
        }
        return resMapList
    }

    /**
     * When useErrorsField this will remove any errors from the list and put them into the errors list.
     * and if success just return the "data" of the result which is stored in result.payload
     * @return the "data", which is the list or payloads for the successful results.
     */
    List<Map> transformResultPayloads(List<Result> resultList) {
        List<Map> resMapList = []
        for (Result r : resultList) {
            if (r instanceof Problem) {
                problems.add(r)
            } else {
                if(r.payload instanceof List){
                    resMapList.addAll( r.payload as List<Map> )
                } else { //assume its a map
                    resMapList.add( r.payload as Map)
                }
            }
        }
        return resMapList
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
     * Update the job with status on whats been processed and append the json data
     * @param currentResults the results to append, normally will be an ApiResults but can be any Problem or Result
     */
    @Synchronized //sync to only one thread for the SyncJob can update at a time
    protected void updateJob(Result currentResults, Map data){
        syncJobService.updateJob(data)
        // append json to dataFile
        if(currentResults) {
            if(args.dataFormat == DataMimeTypes.json) {
                appendDataResults(currentResults)
            } else if(args.dataFormat == DataMimeTypes.csv){
                appendCsv(currentResults)
            }
        }
    }

    /**
     * Append the results. called from updateJob
     * @param currentResults the results to append, normally will be an ApiResults but can be any Problem or Result
     */
    protected void appendDataResults(Result currentResults){
        //if isSaveDataAsFile then write out the results now
        if(args.shouldSaveDataAsFile()){
            boolean isFirstWrite = false
            if(!dataPath) {
                isFirstWrite = true // its first time writing
                initDataFile()
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
            //if not save to file then saves it in memory and will write it out to the prop at finish
            results.merge currentResults
        }

    }

    /**
     * Append the results. called from updateJob
     * @param currentResults the results to append, normally will be an ApiResults but can be any Problem or Result
     */
    protected void appendCsv(Result currentResults){
        boolean isFirstWrite = false
        if(!dataPath) {
            isFirstWrite = true // its first time writing
            initDataFile()
        }
        def writer = dataPath.newWriter(true)
        CSVMapWriter csvWriter = CSVMapWriter.of(writer)
        def dataList = transformResults(currentResults)
        if(isFirstWrite){
            csvWriter.createHeader(dataList)
        } else {
            //csvHeader needs to have headers setup to write.
            csvWriter.setupHeaders(dataList)
        }
        csvWriter.writeCsv(dataList)
        //isFirstWrite = false  //set to false once 1st recod is written with a comma ","
        csvWriter.flush()
        writer.close()
    }

    /**
     * when args.saveDataAsFile = true, this is called to initialize the datafile
     */
    protected void initDataFile() {
        String ext = args.dataFormat as String
        String filename = "SyncJobData_${jobId}_.${ext}"
        dataPath = syncJobService.createTempFile(filename)
        if(args.dataFormat == DataMimeTypes.json){
            //init with the opening brace for JSON
            dataPath.withWriter { wr ->
                wr.write('[\n')
            }
        }
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
