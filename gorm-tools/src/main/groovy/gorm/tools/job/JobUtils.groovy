/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

/**
 * Misc static helpers for SynJobs/ApiJobs
 */
@CompileStatic
class JobUtils {

    static String requestToSourceId(HttpServletRequest req){
        String sourceId = "${req.method} ${req.requestURI}?${req.queryString}"
        return sourceId
    }
}
