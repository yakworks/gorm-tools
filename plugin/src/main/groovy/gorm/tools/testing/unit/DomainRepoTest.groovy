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

    /**
     * By default, calling mockDomains() on {@link DomainRepoTest} will not mock a repository for the specified domain.
     * It will call an inherited {@link grails.buildtestdata.BuildDataTest#mockDomains}.
     *
     * That will cause an error to be thrown for the buildCreate method, because it relies on the create() method in repo.
     *
     * In order to avoid that, an explicit override, which chains to the {@link DataRepoTest#mockDomains} which
     * initializes the repository after mocking the domain) is required.
     */
    @Override
    void mockDomains(Class<?>... domainClassesToMock) {
        DataRepoTest.super.mockDomains(domainClassesToMock)
    }
}
