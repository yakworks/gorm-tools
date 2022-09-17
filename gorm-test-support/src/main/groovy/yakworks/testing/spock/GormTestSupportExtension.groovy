/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.spock

import groovy.transform.CompileStatic

import org.spockframework.runtime.extension.IGlobalExtension
import org.spockframework.runtime.model.SpecInfo

import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.grails.GrailsAppUnitTest

/**
 * See Spock docs. This is a service reference in META-INF/services
 * much easier and more reliable to do setup/cleaup here and make it a trait.
 */
@CompileStatic
class GormTestSupportExtension implements IGlobalExtension {

    GormHibernateTestInterceptor hibernateSpecInterceptor = new GormHibernateTestInterceptor()
    GrailsAppUnitTestInterceptor grailsAppUnitTestInterceptor = new GrailsAppUnitTestInterceptor()

    @Override
    void visitSpec(SpecInfo spec) {
        if (GrailsAppUnitTest.isAssignableFrom(spec.reflection)) {
            spec.addSetupInterceptor(grailsAppUnitTestInterceptor)
            spec.addCleanupSpecInterceptor(grailsAppUnitTestInterceptor)
        }

        if (GormHibernateTest.isAssignableFrom(spec.reflection)) {
            // spec.addInterceptor(hibernateSpecInterceptor)
            spec.addSetupSpecInterceptor(hibernateSpecInterceptor)
            spec.addSetupInterceptor(hibernateSpecInterceptor)
            spec.addCleanupInterceptor(hibernateSpecInterceptor)
            spec.addCleanupSpecInterceptor(hibernateSpecInterceptor)
        }
    }
}
