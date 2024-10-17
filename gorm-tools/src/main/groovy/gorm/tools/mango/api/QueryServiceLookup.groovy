/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango.api

import groovy.transform.CompileStatic

import org.springframework.core.ResolvableType

import gorm.tools.mango.DefaultQueryService
import yakworks.spring.AppCtx

@CompileStatic
class QueryServiceLookup {

    /**
     * Does a lookup to find QueryService for entity.
     * Returns a new DefaultQueryService if none found.
     * NOTE: the DefaultGormRepo beans that are setup when there is no concrete Repo dont retain the D generic
     * So if trying to do @Autowired QueryService<D> fails because it does QueryService<?>, and if multiple QueryService beans
     * are setup then it can't figure out which one because it cant match on the D generic.
     */
    static <D> QueryService<D> lookup(Class<D> entityClass){
        var rt = ResolvableType.forClassWithGenerics(QueryService, entityClass)
        var qsLookup = AppCtx.ctx.getBeanProvider(rt).getIfAvailable() as QueryService<D>
        return qsLookup ?: DefaultQueryService.of(entityClass)
    }

    // static <D,S> S<D> lookupService(Class<D> entityClass, Class<S<D>> serviceClass, String factoryMethod){
    //     var rt = ResolvableType.forClassWithGenerics(serviceClass, entityClass)
    //     var ctx = AppCtx.ctx
    //     var qsLookup = ctx.getBeanProvider(rt).getIfAvailable() as S<D>
    //     if(qsLookup){
    //         return qsLookup
    //     } else {
    //         return ctx.getBean(factoryMethod, [entityClass] as Object[]) as S<D>
    //     }
    // }

    // ResolvableType resolvableType = ResolvableType.forClass(getEntityClass());
    // QueryService<?> myService = queryServiceProvider.getIfAvailable(
    //     (bean) -> ResolvableType.forClass(bean.getClass())
    //         .getInterfaces()[0] // Assuming MyService is the first interface
    //         .getGeneric(0) // Get the generic type parameter
    //         .isAssignableFrom(resolvableType)
    // );
}
