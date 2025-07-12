/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

//import javax.servlet.http.HttpServletRequest

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import com.fasterxml.jackson.databind.util.RawValue
import yakworks.etl.DataMimeTypes

/**
 * Misc static helpers for SynJobs/ApiJobs
 */
@CompileStatic
class JobUtils {

    static String requestToSourceId(Object req){
        String sourceId = "${req['method']} ${req['requestURI']}"
        if(req['queryString']) sourceId = "${sourceId}?${req['queryString']}"
        return sourceId
    }

    // static JsonOutput.JsonUnescaped getRowJobData(SyncJobEntity job) {
    //     // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
    //     String jobData = job.dataToString()
    //     return JsonOutput.unescaped(jobData)
    // }

    /**
     * Use for the Grails controllers that use the Groovy JSON engine.
     * Using special for Groovy vs Jackson so we can send the parser specific unescaped json string in data
     * which for Groovy is the JsonUnescaped class.
     */
    static Map jobToMapGroovy(SyncJobEntity job){
        Map resp = commonJobToMap(job)

        //include job data if job is finished
        if(job.isFinshedAndJson()) {
            resp['data'] =  JsonOutput.unescaped(job.dataToString())
        }
        return resp
    }

    /**
     * Use for the Spring controllers that use the Jackson JSON engine.
     * Using special for Groovy vs Jackson so we can send the parser specific unescaped json string in data,
     * which for jackson is the RawValue class
     */
    static Map jobToMapJackson(SyncJobEntity job){
        Map resp = commonJobToMap(job)

        //include job data if job is finished
        if(job.isFinshedAndJson()){
            resp['data'] =  new RawValue(job.dataToString())
        }
        return resp
    }

    static Map commonJobToMap(SyncJobEntity job){
        Map map = [
            id: job['id'],
            ok: job.ok,
            state: job.state.name(),
            jobType: job.jobType,
            message: job.message,
            source: job.source,
            sourceId: job.sourceId,
            dataFormat: job.dataFormat.name(),
            dataId: job.dataId,
            payloadId: job.payloadId
        ] as Map<String, Object>

        if(job.problems) {
            map['problems'] = job.problems
        }
        return map
    }
}
