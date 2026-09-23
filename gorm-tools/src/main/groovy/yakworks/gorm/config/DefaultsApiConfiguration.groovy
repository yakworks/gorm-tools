/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.config

import java.util.function.Function

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.core.ResolvableType

import yakworks.gorm.api.CrudApi
import yakworks.gorm.api.DefaultCrudApi
import yakworks.gorm.api.bulk.BulkExportService
import yakworks.gorm.api.bulk.BulkImportService
import yakworks.gorm.api.bulk.BulkImporter

@CompileStatic
@Configuration @Lazy(false)
class DefaultsApiConfiguration {

    // @Autowired
    // private ApplicationContext appCtx;

    /**
     * Factory function to return bean if one exists or create one with the defaultCrudApi prototype method
     *
     * <pre>{@code
     * // example usage
     *
     * CrudApi<D> crudApi
     * @Autowired Function<Class, CrudApi> crudApiFactory
     *
     * CrudApi<D> getCrudApi(){
     *   if (!crudApi) {
     *     this.crudApi = crudApiFactory.apply(getEntityClass())
     *   }
     *   return crudApi
     * }
     *
     * }</pre>
     *
     */
    //This is here as reference to show alternate way to do the ServiceLookup.lookup
    // @Bean
    // public <D> Function<Class<D>, CrudApi<D>> crudApiFactory() {
    //     return (Class<D> clazz) -> {
    //         var rt = ResolvableType.forClassWithGenerics(CrudApi.class, clazz);
    //         return (CrudApi<D>) appCtx.getBeanProvider(rt).getIfAvailable(
    //             () -> defaultCrudApi(clazz)
    //         );
    //     }
    // }

    @Bean
    @Scope("prototype")
    //@ConditionalOnMissingBean
    public <D> DefaultCrudApi<D> defaultCrudApi(Class<D> entityClass) {
        return new DefaultCrudApi(entityClass);
    }


    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public <D> BulkImporter<D> defaultBulkImporter(Class<D> entityClass) {
        return new BulkImporter(entityClass);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public <D> BulkImportService<D> defaultBulkImportService(Class<D> entityClass) {
        return new BulkImportService(entityClass);
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public <D> BulkExportService<D> defaultBulkExportService(Class<D> entityClass) {
        return new BulkExportService(entityClass);
    }

    // @Bean
    // @ConditionalOnMissingBean
    // DefaultBulkJobQueueListener bulkJobQueueListener() {
    //     return new DefaultBulkJobQueueListener()
    // }

}
