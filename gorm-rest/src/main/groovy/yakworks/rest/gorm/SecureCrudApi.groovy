/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import groovy.transform.CompileStatic

import org.springframework.security.access.prepost.PreAuthorize

import gorm.tools.job.SyncJobEntity
import gorm.tools.repository.model.DataOp
import yakworks.gorm.api.DefaultCrudApi

@CompileStatic
class SecureCrudApi<D> extends DefaultCrudApi<D> {

    SecureCrudApi(Class<D> entityClass) {
        super(entityClass)
    }

    @Override
    CrudApiResult<D> get(Serializable id, Map params) {
        return super.get(id, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> create(Map data, Map params) {
        return super.create(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> update(Map data, Map params) {
        return super.update(data, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    CrudApiResult<D> upsert(Map data, Map params) {
        return super.update(data, params)
    }

    @Override
    void removeById(Serializable id, Map params) {
        super.removeById(id, params)
    }

    @Override
    @PreAuthorize("!hasRole('ROLE_READ_ONLY')")
    SyncJobEntity bulk(DataOp dataOp, List<Map> dataList, Map params, String sourceId) {
        return super.bulk(dataOp, dataList, params, sourceId)
    }

}
