package gorm.tools.testing.unit

import grails.buildtestdata.TestData
import spock.lang.Specification

/**
 * executes a series automatic "sanity" checks on the domain and repo for CRUD.
 * Allow methods to be overriden for tweaks to build the binding map and asserts
 */
//@CompileStatic
abstract class DomainRepoCrudSpec<D> extends Specification implements DomainRepoTest<D> {
//order on the above Traits is important as both have mockDomains and we want the one in DataRepoTest to be called

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
