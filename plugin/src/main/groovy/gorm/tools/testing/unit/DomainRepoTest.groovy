/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.unit

import groovy.transform.CompileDynamic

import grails.buildtestdata.BuildDataTest

/**
 * Should works as a drop in replacement for the Grails Testing Support's
 * grails.testing.gorm.DomainUnitTest for testing a single entity using Generics
 * Its walks the tree so if you have a Book that has a required Author association you only need to do
 * implement DomainRepoTest<Book> and it will take care of mocking the Author for you.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileDynamic
trait DomainRepoTest<D> implements BuildDataTest, DataRepoTest, DomainCrudSpec<D> {
    //order on the above Traits is important as both have mockDomains and we want the one in DataRepoTest to be called

    /**
     * this is called by the {@link org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor} which calls the mockDomains.
     */
    @Override
    Class<?>[] getDomainClassesToMock() {
        //getEntityClass in BuildDomainTest get the generic on the class
        [entityClass].toArray(Class)
    }


}
