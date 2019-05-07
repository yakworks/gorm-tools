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
 */
@CompileDynamic
abstract class DomainRepoCrudSpec<D> extends Specification implements DomainRepoTest<D> {

    def "create tests"() {
        expect:
        testCreate()
    }

    def "update tests"() {
        expect:
        testUpdate()
    }

    def "persist tests"() {
        expect:
        testPersist()
    }

    def "remove tests"() {
        expect:
        testRemove()
    }

}
