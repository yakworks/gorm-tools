/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.stereotype.Component

import gorm.tools.job.JobUtils
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
    Map entityToMap(SyncJob job, List<String> includes){
        Map response = JobUtils.convertToMap(job)
        return response
    }

    static String requestToSourceId(HttpServletRequest req){
        JobUtils.requestToSourceId(req)
    }

}
