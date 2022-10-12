/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import yakworks.openapi.gorm.OpenApiGenerator
import yakworks.rest.gorm.RestApiFromConfig
import yakworks.rest.grails.AppInfoBuilder

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
@ComponentScan(['yakworks.security', 'yakworks.rally', 'yakworks.testing.gorm.model'])
@Import([AppConfiguration])
@RestApiFromConfig
// caching will use hazelcast for spring caching too, look into how to use caffiene for spring stuff and hazel for hibernate.
@EnableCaching
// this does not appear to be needed if hibernateDatastore depends on hazelcastInstance, see below hack
// might want to do this if we are trying to force cache to use caffiene and only use hazel for hibernate.
// @EnableAutoConfiguration(exclude = [HazelcastAutoConfiguration]) // in order to avoid autoconfiguring an extra Hazelcast instance
@CompileStatic
class Application extends GrailsAutoConfiguration {

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /*
    grails HibernateDatastoreConnectionSourcesRegistrar.postProcessBeanDefinitionRegistry sets up the dataSource but checks for existing first.
    in order for other spring boot Autoconfigure (such as actuator metrics) to get picked up then datasource needs to be setup early.
    it looks like best solution is to fork or modify the hibernate5 plugin so it uses AutoConfgure and can participate in the normal sboot process
    so instead of using HibernateDatastoreSpringInitializer it can autoconfigure the dataSourceConnectionSourceFactory(CachedDataSourceConnectionSourceFactory)
    and the Datasource early (which looks like it should be a factory based on dataSourceConnectionSourceFactory)
    this is a WIP example.
    */
    @Bean // @Primary
    // DataSource dataSource() {
    //     return DataSourceBuilder
    //         .create()
    //         .username("sa")
    //         .password("")
    //         .url("jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE")
    //         .driverClassName("org.h2.Driver")
    //         .build();
    // }

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
        super.packageNames() + ['yakworks.rally', 'yakworks.security', 'yakworks.testing.gorm.model']
    }

    @Bean
    AppInfoBuilder appInfoBuilder() {
        return new AppInfoBuilder()
    }

    @SuppressWarnings('Indentation')
    @Override
    @CompileDynamic
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
        //is configured to join the name of one already setup in spring.
        def hibernateDatastoreBeanDef = getBeanDefinition('hibernateDatastore')
        if (hibernateDatastoreBeanDef) {
            // make it depend on my bean
            hibernateDatastoreBeanDef.dependsOn = ['hazelcastInstance'] as String[]
        }

    }}

}
