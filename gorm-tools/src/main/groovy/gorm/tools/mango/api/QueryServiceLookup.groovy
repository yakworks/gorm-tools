/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import org.springframework.core.ResolvableType

import gorm.tools.mango.DefaultQueryService
import yakworks.spring.AppCtx

class QueryServiceLookup {

    static <D> QueryService<D> lookup(Class<D> entityClass){
        var rt = ResolvableType.forClassWithGenerics(QueryService, entityClass)
        var qsLookup = AppCtx.ctx.getBeanProvider(rt).getIfAvailable() as QueryService<D>
        return qsLookup ?: DefaultQueryService.of(entityClass)
    }
}
