/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import groovy.transform.CompileStatic

import org.springframework.security.access.prepost.PreAuthorize

import gorm.tools.job.SyncJobEntity
import yakworks.gorm.api.CrudApi
import yakworks.gorm.api.bulk.BulkImportJobArgs

@CompileStatic
class SecureCrudApi<D> implements CrudApi<D> {

    @Delegate
    CrudApi<D> defaultCrudApi

    SecureCrudApi(CrudApi<D> defaultCrudApi) {
        this.defaultCrudApi = defaultCrudApi
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> create(Map data, Map params) {
        return defaultCrudApi.create(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> update(Map data, Map params) {
        return defaultCrudApi.update(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> upsert(Map data, Map params) {
        return defaultCrudApi.upsert(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    void removeById(Serializable id, Map params) {
        defaultCrudApi.removeById(id, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    SyncJobEntity bulkImport(BulkImportJobArgs jobParams, List<Map> dataList){
        defaultCrudApi.bulkImport(jobParams, dataList)
    }

}
