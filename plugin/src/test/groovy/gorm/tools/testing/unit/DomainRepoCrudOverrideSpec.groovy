package gorm.tools.testing.unit

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
    CrudSpecDomain buildPersist(Map args) {
        build(save:false, name: 'persist')
    }

    @Override
    void testUpdate() {
        methodCalls << 'testUpdate'
        CrudSpecDomain ent = updateEntity()
        //it should have used the data in the overriden buildUpdateMap
        assert ent.name == 'create foo'
    }

    @Override
    void testCreate() {
        methodCalls << 'testCreate'
        CrudSpecDomain ent = createEntity(firstName: 'billy', lastName: 'bob')
        assert ent.firstName == 'billy' && ent.lastName == 'bob'
        assert ent.name == 'billy bob'
    }

    @Override
    void testPersist() {
        methodCalls << 'testPersist'
        //it should use the data in the overriden buildPersist
        CrudSpecDomain ent = persistEntity()
        assert ent.name == 'persist'
    }

    @Override
    void testRemove() {
        methodCalls << 'testRemove'
        assert removeEntity()
    }

    //show data table option 1
    def "test with data tables"() {
        when:
        def ent = createEntity(params)

        then:
        subsetEquals(expected, ent.properties)

        where:
        params             | expected
        [firstName: 'foo'] | [name: 'foo']
    }

    //show data table option 2
    def "test with data tables cleaner table"() {
        when:
        def ent = createEntity(firstName: firstName, lastName: lastName )

        then:
        ent.name == name

        where:
        firstName | lastName | name
        'foo'     | 'bar'    | 'foo bar'
        'foo'     | null     | 'foo'
    }

}



