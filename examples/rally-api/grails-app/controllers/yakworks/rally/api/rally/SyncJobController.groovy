/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api.rally

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.rally.job.SyncJob
import yakworks.rest.gorm.controller.CrudApiController

import static gorm.tools.repository.RepoUtil.checkFound

@Slf4j
@CompileStatic
class SyncJobController implements CrudApiController<SyncJob> {
    static String namespace = 'rally'

    //action to pull data
    def data() {
        SyncJob syncJob = SyncJob.repo.getNotNull(params.id as Serializable)
        render([text: syncJob.dataToString(), contentType: "application/json", encoding: "UTF-8"])
    }

    //action to pull payload
    def payload() {
        SyncJob syncJob = SyncJob.repo.getNotNull(params.id as Serializable)
        render(text: syncJob.payloadToString(), contentType: "application/json", encoding: "UTF-8")
    }

    //action to pull payload
    def problems() {
        SyncJob syncJob = SyncJob.read(params.id as Serializable)
        checkFound(syncJob, params.id as Serializable, SyncJob.simpleName)
        respond(syncJob.problems ?: [])
    }

    // def errorString() {
    //     SyncJob syncJob = SyncJob.read(params.id as Serializable)
    //     respond([content: syncJob.errorToString()])
    // }
}
