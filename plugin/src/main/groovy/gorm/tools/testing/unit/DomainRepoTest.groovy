package gorm.tools.testing.unit

import gorm.tools.testing.TestDataJson
import gorm.tools.testing.TestTools
import grails.buildtestdata.BuildDataTest
import grails.buildtestdata.TestData
import groovy.transform.CompileDynamic
import org.springframework.core.GenericTypeResolver

/**
 * Should works as a drop in replacement for the Grails Testing Support's
 * grails.testing.gorm.DomainUnitTest for testing a single entity using Generics
 * Its walks the tree so if you have a Book that has a required Author association you only need to do
 * implement DomainRepoTest<Book> and it will take care of mocking the Author for you.
 */
@CompileDynamic
trait DomainRepoTest<D> implements BuildDataTest, DataRepoTest{
//order on the above Traits is important as both have mockDomains and we want the one in DataRepoTest to be called

    D entity
    Class<D> _entityClass


    Class<D> getEntityClass() {
        if (!_entityClass)
            this._entityClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DomainRepoTest.class)
        return _entityClass
    }

    /**
     * this is called by the {@link org.grails.testing.gorm.spock.DataTestSetupSpecInterceptor} which calls the mockDomains.
     */
    @Override
    Class<?>[] getDomainClassesToMock() {
        //getEntityClass in BuildDomainTest get the generic on the class
        [entityClass].toArray(Class)
    }

    /************************ Helpers Methods for expect or then spock blocks *************/
    //keep in mind that is recomended that the inserts be inside the helper methods
    //so basically these blow up on assert fails and give an informative trace with the assert console
    //otherwise they simply execute cleanly

    //Called in concrete implemenations. override these to customize or disable
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

    D get(id){
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

    def removeEntity(remId = null){
        def id = remId ?: persistEntity().id
        get(id).remove()
        flushAndClear()
        assert entityClass.get(id) == null
        return id
    }

}
