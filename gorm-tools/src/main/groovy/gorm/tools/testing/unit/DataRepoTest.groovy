/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileStatic

import gorm.tools.testing.support.ExternalConfigAwareSpec
import gorm.tools.testing.support.GormToolsSpecHelper
import gorm.tools.testing.support.JsonViewSpecSetup
import grails.buildtestdata.BuildDataTest
import grails.testing.spring.AutowiredTest

/**
 * Spec trait to use as a drop in replacement of DataTest and GormToolsTest that has all the methods
 * from the BuildDataTest to build data for the repos
 * will set up the repositories properly for the mocked domains
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait DataRepoTest implements JsonViewSpecSetup, GormToolsSpecHelper, BuildDataTest, AutowiredTest, ExternalConfigAwareSpec {

    void mockDomains(Class<?>... domainClassesToMock) {
        BuildDataTest.super.mockDomains(domainClassesToMock)
        defineRepoBeans()
        setupValidatorRegistry()
    }

    //called from BuildDataTest as it setups and mocks the domains
    void onMockDomains(Class<?>... entityClasses) {
        defineBeans(doWithSpringFirst())
        //mockRepositories(entityClasses)
    }

    /**
     * Call back to provide beans before repositories are mocked, this gives chance to define beans which may need to
     * be injected into repositories
     */
    Closure doWithSpringFirst() {
        return {}
    }
}
