package gorm.tools.testing.unit

import gorm.tools.testing.TestDataJson
import grails.buildtestdata.BuildDomainTest
import groovy.transform.CompileStatic

/**
 * Should works as a drop in replacement for the Grails Testing Support's
 * grails.testing.gorm.DomainUnitTest for testing a single entity using Generics
 * Its walks the tree so if you have a Book that has a required Author association you only need to do
 * implement BuildDomainTest<Book> and it will take care of mocking the Author for you.
 */
@CompileStatic
trait DomainRepoTest<D> implements DataRepoTest, BuildDomainTest<D> {

    Map buildMap(Map args = [:]) {
        TestDataJson.buildMap(args, getEntityClass())
    }

    D buildCreate(Map args = [:]) {
        TestDataJson.buildCreate(args, getEntityClass())
    }
}
