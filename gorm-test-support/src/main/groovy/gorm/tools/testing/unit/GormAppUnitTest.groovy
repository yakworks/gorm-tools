/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileStatic

import gorm.tools.testing.support.GormToolsSpecHelper
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
trait GormAppUnitTest implements AutowiredTest, BuildDataTest, GormToolsSpecHelper {

    /**
     * Overrides the BuildDataTest so we can register the repo beans after mock
     */
    @Override //BuildDataTest
    void mockDomains(Class<?>... domainClassesToMock) {
        BuildDataTest.super.mockDomains(domainClassesToMock)
        defineRepoBeans(domainClassesToMock)
        setupValidatorRegistry()

    }

}
