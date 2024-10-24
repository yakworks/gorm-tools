/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import groovy.transform.CompileStatic

import grails.buildtestdata.testing.DependencyDataTest

/**
 * Unit tests should implement this trait to add build-test-data functionality.
 * Meant as a drop in replacement for Grails Testing Support's DataTest
 */
@CompileStatic
@SuppressWarnings("GroovyUnusedDeclaration")
trait RepoBuildDataTest extends DependencyDataTest implements RepoTestDataBuilder {

    void mockDomainsBuildDataTest(Class<?>... domainClassesToMock) {
        DependencyDataTest.super.mockDomains(domainClassesToMock)

        // Add build methods
        // Class[] domainClasses = getDatastore().mappingContext.getPersistentEntities()*.javaClass
        // MetaHelper.addBuildMetaMethods(domainClasses)
    }

}
