/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.support

import groovy.transform.CompileDynamic

import org.springframework.core.GenericTypeResolver

import gorm.tools.testing.TestDataJson
import gorm.tools.testing.TestTools
import grails.buildtestdata.TestData

/**
 * Provides CRUD tests to automatically unit test domains
 *
 */
@CompileDynamic
trait DomainCrudSpec<D> {
    D entity
    Class<D> _entityClass

    Class<D> getEntityClass() {
        if (!_entityClass) {
            this._entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainCrudSpec)
        }

        assert _entityClass != null, "No Generic type found for ${getClass().simpleName}"
        return _entityClass
    }

    void testCreate(){
        assert createEntity().id
    }
    void testUpdate(){
        assert updateEntity().version > 0
    }
    void testPersist(){
        assert persistEntity().id
    }
    void testRemove(){
        assert removeEntity()
    }

    /************************ Helpers Methods for expect or then spock blocks *************/
    //keep in mind that is recomended that the inserts be inside the helper methods
    //so basically these blow up on assert fails and give an informative trace with the assert console
    //otherwise they simply execute cleanly

    /** asserts that the entity's props contains the expected map */
    void entityContains(Map expected){
        assert TestTools.entityContains(entity, expected)
    }

    /************************ builders, util and setup methods for spock blocks *************/

    D build() {
        entity = TestData.build([:], entityClass)
        entity
    }

    D build(Map args) {
        entity = TestData.build(args, entityClass)
        entity
    }

    Map buildMap(Map args = [:]) {
        TestDataJson.buildMap(args, entityClass)
    }

    Map buildCreateMap(Map args) {
        buildMap(args)
    }

    Map buildUpdateMap(Map args) {
        buildMap(args)
    }

    D get(Serializable id){
        flushAndClear()
        entity = entityClass.get(id)
        assert entity
        return entity
    }

    D createEntity(Map args = [:]){
        entity = entityClass.create(buildCreateMap(args))
        //assert entity.properties == [foo:'foo']
        return get(entity.id)
    }

    D updateEntity(Map args = [:]){
        def id = args.id ? args.remove('id') : createEntity().id
        Map updateMap = buildUpdateMap(args)
        updateMap.id = id
        assert entityClass.update(updateMap)
        return get(id)
    }

    D persistEntity(Map args = [:]){
        args.get('save', false) //adds save:false if it doesn't exists
        entity = build(args)
        assert entity.persist(flush: true)
        return get(entity.id)
    }

    def removeEntity(Serializable remId = null){
        Serializable id = remId ?: persistEntity().id
        get(id).remove()
        flushAndClear()
        assert entityClass.get(id) == null
        return id
    }
}
