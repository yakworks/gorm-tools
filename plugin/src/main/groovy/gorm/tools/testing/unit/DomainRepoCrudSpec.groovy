/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileDynamic

import spock.lang.Specification

/**
 * executes a series automatic "sanity" checks on the domain and repo for CRUD.
 * Allow methods to be overriden for tweaks to build the binding map and asserts
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
abstract class DomainRepoCrudSpec<D> extends Specification implements DomainRepoTest<D> {

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
