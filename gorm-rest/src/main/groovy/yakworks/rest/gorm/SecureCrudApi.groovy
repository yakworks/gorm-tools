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
    private CrudApi<D> crudApi

    SecureCrudApi(CrudApi<D> d) {
        this.crudApi = d
    }

    @Override
    @PreAuthorize("!hasRole('READ_ONLY')")
    CrudApiResult<D> get(Serializable id, Map params) {
        return crudApi.get(id, params)
    }

    @Override
    @PreAuthorize("!hasRole('READ_ONLY')")
    CrudApiResult<D> create(Map data, Map params) {
        return crudApi.create(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('READ_ONLY')")
    CrudApiResult<D> update(Map data, Map params) {
        return crudApi.update(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('READ_ONLY')")
    CrudApiResult<D> upsert(Map data, Map params) {
        return crudApi.update(data, params)
    }

    @Override
    void removeById(Serializable id, Map params) {
        crudApi.removeById(id, params)
    }

    @Override
    @PreAuthorize("!hasRole('READ_ONLY')")
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map params, String sourceId) {
        return crudApi.bulk(dataOp, dataList, params, sourceId)
    }

}
