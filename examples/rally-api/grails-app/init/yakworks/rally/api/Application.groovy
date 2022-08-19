/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

import gorm.tools.rest.RestApiFromConfig
import gorm.tools.rest.appinfo.AppInfoBuilder
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import yakworks.gorm.oapi.OpenApiGenerator

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
@ComponentScan(['yakworks.security', 'gorm.tools.security', 'yakworks.rally', 'yakworks.gorm.testing.model'])
@RestApiFromConfig
@EnableCaching
// @EnableAutoConfiguration(exclude = [HazelcastAutoConfiguration]) // in order to avoid autoconfiguring an extra Hazelcast instance
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * To scan and pick up the gorm domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    /**
     * add packages here where the other grails artifacts exist such as domains marked with @Entity
     */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + ['yakworks.rally', 'gorm.tools.security', 'yakworks.gorm.testing.model']
    }

    @Bean
    AppInfoBuilder appInfoBuilder() {
        return new AppInfoBuilder()
    }

    @SuppressWarnings('Indentation')
    @Override
    Closure doWithSpring() {{ ->
        appInfoBuilder(AppInfoBuilder)

        // openApiGenerator(OpenApiGenerator) { bean ->
        //     bean.lazyInit = true
        //     apiSrc = 'api-docs/openapi'
        //     apiBuild = 'api-docs/dist/openapi'
        //     namespaceList = ['rally']
        // }
        openApiGenerator(OpenApiGenerator) { bean ->
            bean.lazyInit = true
            apiSrc = 'api-docs/openapi'
            apiBuild = 'api-docs/dist/openapi'
            namespaceList = ['rally']
        }

        //hack to make sure hazel get setup before the one that is setup for hibernates L2 cache as that one
        //is configured to join the name of the one setup in spring.
        def hibernateDatastoreBeanDef = getBeanDefinition('hibernateDatastore')
        if (hibernateDatastoreBeanDef) {
            // make it depend on my bean
            hibernateDatastoreBeanDef.dependsOn = ['hazelcastInstance'] as String[]
        }

    }}

}
