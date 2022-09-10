/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.unit

import groovy.transform.CompileStatic

import org.junit.AfterClass

import gorm.tools.beans.AppCtx
import grails.testing.spring.AutowiredTest
import yakworks.gorm.testing.support.ExternalConfigAwareSpec
import yakworks.gorm.testing.support.GormToolsSpecHelper

/**
 * Spec trait to use as a drop in replacement of DataTest and GormToolsTest that has all the methods
 * from the BuildDataTest to build data for the repos
 * will set up the repositories properly for the mocked domains
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait DataRepoTest implements GormToolsSpecHelper, RepoBuildDataTest, AutowiredTest, ExternalConfigAwareSpec  {

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

}
