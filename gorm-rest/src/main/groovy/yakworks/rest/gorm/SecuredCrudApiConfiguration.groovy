/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import groovy.transform.CompileStatic

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

import gorm.tools.utils.ServiceLookup
import yakworks.gorm.api.CrudApi

@CompileStatic
@Configuration
class SecuredCrudApiConfiguration {

    @Bean
    @Scope("prototype")
    <D> SecureCrudApi<D> secureCrudApi(Class<D> entityClass) {
        CrudApi<D> defaultCrudApi = ServiceLookup.lookup(entityClass, CrudApi<D>, "defaultCrudApi")
        return new SecureCrudApi<D>(defaultCrudApi)
    }

}
