/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileStatic

import grails.testing.spring.AutowiredTest
import yakworks.testing.gorm.support.GormToolsSpecHelper

/**
 * Spec trait to use as a drop in replacement of DataTest and GormToolsTest that has all the methods
 * from the RepoBuildDataTest to build data for the repos
 * will set up the repositories properly for the mocked domains
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait GormAppUnitTest implements AutowiredTest, RepoBuildDataTest, GormToolsSpecHelper {

    /**
     * Overrides the RepoBuildDataTest so we can register the repo beans after mock
     */
    @Override //RepoBuildDataTest
    void mockDomains(Class<?>... domainClassesToMock) {
        mockDomainsBuildDataTest(domainClassesToMock)
        defineRepoBeans(domainClassesToMock)
        setupValidatorRegistry()
    }

}
