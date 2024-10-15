/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.transform.CompileStatic

import org.springframework.core.ResolvableType

import gorm.tools.mango.DefaultQueryService
import gorm.tools.mango.api.QueryService
import yakworks.spring.AppCtx

@CompileStatic
class ServiceLookup {

    /**
     * find bean with entityClass generic or call factory method if not found
     *
     * @param entityClass the entity class generid
     * @param serviceClass the Class with the entityClass as the generic
     * @param factoryMethod the name of the bean method
     * @return the bean
     */
    static <D,S> S<D> lookup(Class<D> entityClass, Class<S<D>> serviceClass, String factoryMethod){
        var rt = ResolvableType.forClassWithGenerics(serviceClass, entityClass)
        return (S<D>)AppCtx.ctx.getBeanProvider(rt).getIfAvailable(
            () -> AppCtx.ctx.getBean(factoryMethod, [entityClass] as Object[])
        )
    }

    // ResolvableType resolvableType = ResolvableType.forClass(getEntityClass());
    // QueryService<?> myService = queryServiceProvider.getIfAvailable(
    //     (bean) -> ResolvableType.forClass(bean.getClass())
    //         .getInterfaces()[0] // Assuming MyService is the first interface
    //         .getGeneric(0) // Get the generic type parameter
    //         .isAssignableFrom(resolvableType)
    // );
}
