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
