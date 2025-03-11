/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import groovy.transform.CompileStatic

import org.springframework.security.access.prepost.PreAuthorize

import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.model.DataOp
import yakworks.gorm.api.CrudApi

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
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map params, String sourceId) {
        return defaultCrudApi.bulk(dataOp, dataList, params, sourceId)
    }

}
