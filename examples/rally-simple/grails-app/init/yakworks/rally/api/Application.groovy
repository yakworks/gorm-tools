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
import yakworks.rally.RallyConfiguration
import yakworks.rest.gorm.RestApiFromConfig

// the component scan here does not seem to be the same as the packageNames and is needed to pick up the
// the services marked with @Component
@Import([RallyApiSpringConfig])
@ComponentScan(['yakworks.testing.gorm.model'])
@RestApiFromConfig
// caching will use hazelcast for spring caching too, look into how to use caffiene for spring stuff and hazel for hibernate.
@EnableCaching
// this does not appear to be needed if hibernateDatastore depends on hazelcastInstance, see below hack
// might want to do this if we are trying to force cache to use caffiene and only use hazel for hibernate.
// @EnableAutoConfiguration(exclude = [HazelcastAutoConfiguration]) // in order to avoid autoconfiguring an extra Hazelcast instance
@CompileStatic
class Application extends GrailsAutoConfiguration {

    /** add packages here where the @Entity classes are */
    @Override
    Collection<String> packageNames() {
        super.packageNames() + RallyConfiguration.entityScanPackages + ['yakworks.testing.gorm.model']
    }

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    /**
     * To scan and pick up the gorm domains that are marked with @entity
     * outside of the package this Application class is in then this needs to be set to true
     */
    @Override
    protected boolean limitScanningToApplication() { false }

    @SuppressWarnings('Indentation')
    @Override
    @CompileDynamic
    Closure doWithSpring() {{ ->

    }}

}
