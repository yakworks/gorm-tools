/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import groovy.transform.CompileStatic

import org.junit.AfterClass

import yakworks.commons.beans.PropertyTools
import yakworks.spring.AppCtx
import yakworks.testing.gorm.support.BaseRepoEntityUnitTest
import yakworks.testing.gorm.support.RepoBuildDataTest
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * Spec trait to use as a drop in replacement of DataTest and GormToolsTest that has all the methods
 * from the BuildDataTest to build data for the repos
 * will set up the repositories properly for the mocked domains
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait DataRepoTest
    //implements RepoBuildDataTest, BaseRepoEntityUnitTest, GrailsAppUnitTest {
    implements RepoBuildDataTest, GrailsAppUnitTest, BaseRepoEntityUnitTest {
    //trait order above is important, GormToolsSpecHelper should come last as it overrides methods in GrailsAppUnitTest

    @Override //hack, see explanation on isDataRepoTest in GrailsAppBuilder. has to do with context.'annotation-config'()
    boolean getDataRepoTest(){true}

    void mockDomains(Class<?>... domainClassesToMock) {
        mockDomainsBuildDataTest(domainClassesToMock)
        defineRepoBeans(domainClassesToMock)
        setupValidatorRegistry()
        // this does something to make the events work for security
        // applicationContext.beanFactory.preInstantiateSingletons()
    }

    @AfterClass
    static void cleanupAppCtx() {
        AppCtx.setApplicationContext(null)
    }

    @Override
    Class<?>[] getDomainClassesToMock() {
        def persistentClasses = (PropertyTools.getOrNull(this, 'domainClasses')?:PropertyTools.getOrNull(this, 'entityClasses')) as List<Class>
        return (persistentClasses?:[]) as Class<?>[]
    }

}
