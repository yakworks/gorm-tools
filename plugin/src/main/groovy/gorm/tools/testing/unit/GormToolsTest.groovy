/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileStatic

import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest

/**
 * Specification trait to use as a drop in replacement for DataTest,
 * will set up the repositories properly for the mocked domains.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait GormToolsTest implements GormToolsSpecHelper, DataTest, AutowiredTest {

    /**
     * Mocks domain classes providing the equivalent GORM behavior as well as the Repo for the domain.
     * If a Repository Class is explicitly defined then this looks for it in the same package
     *
     * @param domainClassesToMock The list of domain classes to mock
     */
    @Override
    void mockDomains(Class<?>... domainClassesToMock) {
        DataTest.super.mockDomains(domainClassesToMock)
        mockRepositories(domainClassesToMock)
    }

}
