/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import groovy.transform.CompileStatic

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope

import gorm.tools.repository.bulk.BulkImporter
import yakworks.gorm.api.bulk.BulkExportService
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.gorm.api.bulk.DefaultBulkJobQueueListener

@CompileStatic
@Configuration @Lazy(false)
class DefaultBulkConfiguration {

    // @Autowired
    // private ApplicationContext appCtx;

    @Bean
    @Scope("prototype")
    public <D> BulkImporter<D> defaultBulkImporter(Class<D> entityClass) {
        return new BulkImporter(entityClass);
    }

    @Bean
    @Scope("prototype")
    public <D> BulkImportService<D> defaultBulkImportService(Class<D> entityClass) {
        return new BulkImportService(entityClass);
    }

    @Bean
    @Scope("prototype")
    public <D> BulkExportService<D> defaultBulkExportService(Class<D> entityClass) {
        return new BulkExportService(entityClass);
    }

    @Bean
    @ConditionalOnMissingBean
    DefaultBulkJobQueueListener bulkJobQueueListener() {
        return new DefaultBulkJobQueueListener()
    }
}
