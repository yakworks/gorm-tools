package gorm.tools.testing.unit

import gorm.tools.compiler.GormRepository
import gorm.tools.repository.GormRepo
import grails.persistence.Entity
import spock.lang.Shared
import testing.Org

//test to check overriding methods
class DomainRepoCrudSpecOverridesSpec extends DomainRepoCrudSpec<CrudSpecDomain> {

    //testCreate calls this for the data pipes (tables)
    @Override
    def whereCreate() {
        [[
             params  : [firstName: 'create', lastName: 'foo'],
             expected: [name: 'create foo']
         ],
         [
             params  : [firstName: 'boo'],
             expected: [name: 'boo']
         ]]
    }

    //show data table option 1
    void "test with data tables"() {
        when:
        def ent = createEntity(params)

        then:
        subsetEquals(expected, ent.properties)

        where:
        params             | expected
        [firstName: 'foo'] | [name: 'foo']
    }

    //show data table option 2
    void "test with data tables cleaner table"() {
        when:
        def ent = createEntity(firstName: firstName, lastName: lastName )

        then:
        ent.name == name

        where:
        firstName | lastName | name
        'foo'     | 'bar'    | 'foo bar'
        'foo'     | null     | 'foo'
    }

    //show override for testPersist
    def testPersist() {
        when:
        CrudSpecDomain ent = persistEntity(firstName:'foo')

        then:
        ent.name == 'foo'
    }
}



