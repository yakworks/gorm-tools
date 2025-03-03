/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm

import java.util.function.Function

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.core.ResolvableType

import yakworks.gorm.api.CrudApi
import yakworks.gorm.api.DefaultCrudApi

@CompileStatic
@Configuration
class SecuredCrudApiConfiguration {

    @Bean
    @Scope("prototype")
    public <D> SecureCrudApi<D> secureCrudApi(Class<D> entityClass) {
        return new SecureCrudApi<D>(new DefaultCrudApi(entityClass))
    }

}
