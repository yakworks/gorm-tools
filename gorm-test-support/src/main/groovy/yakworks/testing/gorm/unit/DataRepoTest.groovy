/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.config.PropertySourcesConfig
import org.junit.AfterClass

import gorm.tools.ConfigDefaults
import grails.config.Config
import grails.testing.spring.AutowiredTest
import yakworks.spring.AppCtx
import yakworks.testing.gorm.support.ExternalConfigAwareSpec
import yakworks.testing.gorm.support.GormToolsSpecHelper

/**
 * Spec trait to use as a drop in replacement of DataTest and GormToolsTest that has all the methods
 * from the BuildDataTest to build data for the repos
 * will set up the repositories properly for the mocked domains
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait DataRepoTest implements GormToolsSpecHelper, RepoBuildDataTest, AutowiredTest { //, ExternalConfigAwareSpec  {

    void mockDomains(Class<?>... domainClassesToMock) {
        mockDomainsBuildDataTest(domainClassesToMock)
        defineRepoBeans(domainClassesToMock)
        setupValidatorRegistry()
    }

    @AfterClass
    def cleanupAppCtx() {
        AppCtx.setApplicationContext(null)
    }

    //called from RepoBuildDataTest as it setups and mocks the domains
    // void onMockDomains(Class<?>... entityClasses) {
    //     defineBeans(doWithSpringFirst())
    //     //mockRepositories(entityClasses)
    // }

    @Override
    @CompileDynamic
    Closure doWithConfig() {
        { config ->
            gormConfigDefaults(config)
        }
    }

    PropertySourcesConfig gormConfigDefaults(PropertySourcesConfig config){
        config.putAll(ConfigDefaults.getConfigMap(false))
        return config
    }
}
