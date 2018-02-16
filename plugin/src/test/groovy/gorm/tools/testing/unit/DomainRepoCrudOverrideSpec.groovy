package gorm.tools.testing.unit

import grails.buildtestdata.TestData
import spock.lang.Shared

//test to check the overrride methods
class DomainRepoCrudOverrideSpec extends DomainRepoCrudSpec<CrudSpecDomain> {

    @Shared List methodCalls = []

    def cleanupSpec() {
        assert methodCalls.containsAll(['testUpdate', 'testCreate', 'testPersist', 'testRemove'])
    }

    @Override
    Map buildUpdateMap(Map args) {
        args << [firstName: 'create', lastName: 'foo']
        buildMap(args)
    }

    @Override
    void testUpdate() {
        methodCalls << 'testUpdate'
        updateEntity()
        //it should have used the data in the overriden buildUpdateMap
        assert entity.name == 'create foo'
    }

    @Override
    void testCreate() {
        methodCalls << 'testCreate'
        createEntity(firstName: 'billy', lastName: 'bob')
        assert entity.firstName == 'billy' && entity.lastName == 'bob'
        assert entity.name == 'billy bob'
    }

    @Override
    void testPersist() {
        methodCalls << 'testPersist'
        persistEntity(name: 'persist')
        assert entity.name == 'persist'
    }

    @Override
    void testRemove() {
        methodCalls << 'testRemove'
        assert removeEntity()
    }

    //show data table option 1
    def "test with data tables"() {
        when:
            createEntity(params)
        then:
            entityContains(expected)

        where:
        params             | expected
        [firstName: 'foo'] | [name: 'foo']
    }

    //show data table option 2
    def "test with data tables cleaner table"() {
        when:
            createEntity(firstName: firstName, lastName: lastName )

        then:
            entity.name == name

        where:
        firstName | lastName | name
        'foo'     | 'bar'    | 'foo bar'
        'foo'     | null     | 'foo'
    }

}



