/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import org.springframework.stereotype.Component

import gorm.tools.job.SyncJobEntity
import gorm.tools.job.SyncJobState
import yakworks.api.HttpStatus
import yakworks.api.problem.data.DataProblem
import yakworks.api.problem.data.DataProblemException
import yakworks.commons.map.Maps
import yakworks.gorm.api.DefaultCrudApi
import yakworks.gorm.api.bulk.BulkExportJobArgs
import yakworks.gorm.api.bulk.BulkImportJobArgs

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
    SyncJobEntity bulkImport(BulkImportJobArgs jobParams, List<Map> bodyList){
        throw notSupported("bulk")
    }

    @Override
    SyncJobEntity bulkExport(BulkExportJobArgs jobParams) {
        throw notSupported("bulk")
    }

    private DataProblemException notSupported(String op) {
        throw DataProblem.ex("Syncjob does not support operation '$op'").status(HttpStatus.FORBIDDEN.code)
    }


    @Override
    Map entityToMap(SyncJob job, List<String> includes){
        //clone metamap, SomeHow trying to put a new entry in MetaMap throws UnSupportedOperationException
        Map response = Maps.clone(super.entityToMap(job, includes))
        if(job.isFinshedAndJson()) {
            response.put("data", JsonOutput.unescaped(job.dataToString()))
        }
        // if(includes.contains('data')) {
        //     response.put("data", JsonOutput.unescaped(job.dataToString()))
        // }
        //include problems by default if its not ok.
        if(!job.ok && job.state == SyncJobState.Finished) {
            response.put("problems", job.problems)
        }
        return response
    }

    // static String requestToSourceId(HttpServletRequest req){
    //     JobUtils.requestToSourceId(req)
    // }

}
