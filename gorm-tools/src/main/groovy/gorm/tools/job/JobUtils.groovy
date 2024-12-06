/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

//import javax.servlet.http.HttpServletRequest

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

/**
 * Misc static helpers for SynJobs/ApiJobs
 */
@CompileStatic
class JobUtils {

    @Deprecated //removed the servlet api dependency
    static String requestToSourceId(Object req){
        String sourceId = "${req['method']} ${req['requestURI']}"
        if(req['queryString']) sourceId = "${sourceId}?${req['queryString']}"
        return sourceId
    }

    //static helper as its used both here and also in the SyncJobRenderer
    static Map convertToMap(SyncJobEntity job){
        // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
        String jobData = job.dataToString()
        JsonOutput.JsonUnescaped rawDataJson = JsonOutput.unescaped(jobData)

        Map map = [
            id: job['id'],
            ok: job.ok,
            state: job.state.name(),
            source: job.source,
            sourceId: job.sourceId,
            createdDate: job['createdDate'], //does not have access to Auditstamp trait here
            editedDate: job['editedDate'],
            data: rawDataJson
        ]

        if(job.problems) {
            map['problems'] = job.problems
        }
        return map
    }
}
