/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.unit

import groovy.transform.CompileDynamic

import org.springframework.core.GenericTypeResolver

import yakworks.gorm.testing.RepoTestData
import yakworks.gorm.testing.TestDataJson

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
trait DomainRepoTest<D> implements RepoBuildDataTest, DataRepoTest {
    //order on the above Traits is important as both have mockDomains and we want the one in DataRepoTest to be called

    /**
     * this is called by the {@link org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor} which calls the mockDomains.
     */
    @Override
    Class<?>[] getDomainClassesToMock() {
        //getEntityClass in BuildDomainTest get the generic on the class
        [getEntityClass()].toArray(Class)
    }

    Class<D> _entityClass // the domain class this is for

    /**
     * The gorm domain class. uses the {@link org.springframework.core.GenericTypeResolver} is not set during contruction
     */
    Class<D> getEntityClass() {
        if (!_entityClass) this._entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainRepoTest)
        assert _entityClass != null, "No Generic type found for ${getClass().simpleName}"
        return _entityClass
    }

    /************************ Helpers Methods for expect or then spock blocks *************/
    //keep in mind that is recomended that the inserts be inside the helper methods
    //so basically these blow up on assert fails and give an informative trace with the assert console
    //otherwise they simply execute cleanly

    /************************ builders, util and setup methods for spock blocks *************/

    D build() {
        RepoTestData.build([:], getEntityClass())
    }

    D build(Map args) {
        RepoTestData.build(args, getEntityClass())
    }

    Map buildMap(Map args = [:]) {
        TestDataJson.buildMap(args, getEntityClass())
    }

    Map buildCreateMap(Map args) {
        buildMap(args)
    }

    Map buildUpdateMap(Map args) {
        buildMap(args)
    }

    D get(Serializable id){
        flushAndClear()
        def entity = getEntityClass().get(id)
        assert entity
        return entity
    }

    D createEntity(Map args = [:]){
        def entity = getEntityClass().create(buildCreateMap(args))
        //assert entity.properties == [foo:'foo']
        return get(entity.id)
    }

    D updateEntity(Map args = [:]){
        def id = args.id ? args.remove('id') : createEntity().id
        Map updateMap = buildUpdateMap(args)
        updateMap.id = id
        assert getEntityClass().update(updateMap)
        return get(id)
    }

    D persistEntity(Map args = [:]){
        args.get('save', false) //adds save:false if it doesn't exists
        def entity = build(args)
        assert entity.persist(flush: true)
        return get(entity.id)
    }

    def removeEntity(Serializable remId = null){
        Serializable id = remId ?: persistEntity().id
        get(id).remove()
        flushAndClear()
        assert getEntityClass().get(id) == null
        return id
    }

}
