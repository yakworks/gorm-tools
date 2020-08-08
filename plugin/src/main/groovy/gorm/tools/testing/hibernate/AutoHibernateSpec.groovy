/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.hibernate

import groovy.transform.CompileDynamic

import gorm.tools.testing.support.DomainCrudSpec

/**
 * automatically runs tests on persist(), create(), update(), delete()
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings(['JUnitPublicNonTestMethod', 'JUnitLostTest', 'JUnitTestMethodWithoutAssert'])
@CompileDynamic
abstract class AutoHibernateSpec<D> extends GormToolsHibernateSpec implements DomainCrudSpec<D> {

    @Override
    List<Class<D>> getDomainClasses() { [getEntityClass()] }

    void "create tests"() {
        expect:
        testCreate()
    }

    void "update tests"() {
        expect:
        testUpdate()
    }

    void "persist tests"() {
        expect:
        testPersist()
    }

    void "remove tests"() {
        expect:
        testRemove()
    }

}
