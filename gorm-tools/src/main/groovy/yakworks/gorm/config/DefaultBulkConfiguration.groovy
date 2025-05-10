/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope

import gorm.tools.repository.bulk.BulkImporter

@CompileStatic
@Configuration
@Lazy(false)
class DefaultBulkConfiguration {

    @Autowired
    private ApplicationContext appCtx;

    @Bean
    @Scope("prototype")
    public <D> BulkImporter<D> defaultBulkImporter(Class<D> entityClass) {
        return new BulkImporter(entityClass);
    }
}
