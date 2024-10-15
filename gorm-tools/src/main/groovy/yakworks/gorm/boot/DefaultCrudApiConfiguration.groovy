/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.boot

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope

import yakworks.gorm.api.DefaultCrudApi

@Configuration
@CompileStatic
class DefaultCrudApiConfiguration {

    @Autowired
    ApplicationContext applicationContext

    //factory method for ObjectProvider
    @Bean
    @Scope("prototype")
    public <D> DefaultCrudApi<D> defaultCrudApi(Class<D> entityClass) {
        return new DefaultCrudApi(entityClass)
    }
}
