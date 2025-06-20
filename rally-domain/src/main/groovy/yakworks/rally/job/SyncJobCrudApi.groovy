/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.stereotype.Component

import gorm.tools.job.JobUtils
import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.model.DataOp
import yakworks.api.HttpStatus
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemException
import yakworks.commons.map.Maps
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

    CrudApiResult<SyncJob> create(Map data, Map params) {
        throw notSupported("create")
    }

    CrudApiResult<SyncJob> update(Map data, Map params) {
        throw notSupported("update")
    }


    CrudApiResult<SyncJob> upsert(Map data, Map params) {
        throw notSupported("upsert")
    }


    void removeById(Serializable id, Map params) {
        throw notSupported("delete")
    }

    @Override
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId) {
        throw notSupported("bulk")
    }

    private DataProblemException notSupported(String op) {
        throw DataProblem.ex("Syncjob does not support operation '$op'").status(HttpStatus.FORBIDDEN.code)
    }


    @Override
    Map entityToMap(SyncJob job, List<String> includes){
        //clone metamap, SomeHow trying to put a new entry in MetaMap throws UnSupportedOperationException
        Map response = Maps.clone(super.entityToMap(job, includes))
        response.put("data", JobUtils.getRowJobData(job))
        return response
    }

    static String requestToSourceId(HttpServletRequest req){
        JobUtils.requestToSourceId(req)
    }

}
