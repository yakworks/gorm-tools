/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import org.springframework.stereotype.Component

import yakworks.gorm.api.DefaultCrudApi

/**
 * Used by CrudApiController for rest api.
 */
@Component
@CompileStatic
class SyncJobCrudApi extends DefaultCrudApi<SyncJob> {

    SyncJobCrudApi() {
        super(SyncJob)
    }

    @Override
    Map entityToMap(SyncJob job, Map qParams){
        // gets the raw json string and use the unescaped to it just dumps it to writer without any round robin conversion
        String jobData = job.dataToString()
        JsonOutput.JsonUnescaped rawDataJson = JsonOutput.unescaped(jobData)

        Map response = [
            id: job.id,
            ok: job.ok,
            state: job.state.name(),
            source: job.source,
            sourceId: job.sourceId,
            data: rawDataJson
        ]

        if(job.problems) {
            response['problems'] = job.problems
        }
        return response
    }
}
