package gorm.tools.hibernate.criteria

import grails.test.hibernate.HibernateSpec

class GormHibernateCriteriaBuilderSpec extends HibernateSpec { //c DataTest {//implements DataTest{

//    void setupSpec() {
//        mockDomain Test
//    }

    List<Class> getDomainClasses() { [Test] }

    private GormHibernateCriteriaBuilder builder

    void setup() {
        //SessionFactory sessionFactory = Holders.grailsApplication.mainContext.sessionFactory
        builder = new GormHibernateCriteriaBuilder(Test.class, sessionFactory)
    }

    void "test order"() {
        setup:
        (1..3).each { index ->
            new Test(id: index, someField: "aaa_$index", someField2: "bbb_$index").save()
        }

        when:
        List result = builder.list() {
            order("someField", "desc")
        }
        List expected = Test.createCriteria().list() {
            order("someField", "desc")
        }

        then:
        result.eachWithIndex { Test entity, int i ->
            assert entity.someField == expected[i].someField
        }
    }

    void "test order with a nested property"() {
        setup:
        (0..2).each { index ->
            new Test(id: index, someField: "1", someField2: "2", nestedField: new Test2(someField: "abc_$index").save()).save()
        }

        when:
        List result = builder.list() { order("nestedField.someField", "desc") }

        then:
        result[0].nestedField.someField == "abc_2"
        result[1].nestedField.someField == "abc_1"
        result[2].nestedField.someField == "abc_0"
    }

    void "test ilike"() {
        setup:
        new Test(id: 0, someField: "abc", someField2: "abc").save()
        new Test(id: 1, someField: "aBc", someField2: "abc").save()
        new Test(id: 2, someField: "Bcd", someField2: "bcd").save()

        when:
        List result = builder.list() { ilike('someField', 'aB%') }

        then:
        result.size() == 2
        result[0].someField == "abc"
        result[1].someField == "aBc"

        cleanup:

        Test.executeUpdate('delete from Test')
    }

    void "test like"() {
        setup:
        new Test(id: 0, someField: "abc", someField2: "abc").save()
        new Test(id: 1, someField: "aBc", someField2: "abc").save()
        new Test(id: 2, someField: "Bcd", someField2: "bcd").save()

        when:
        List result = builder.list() { like('someField', 'aB%') }

        then:
        result.size() == 1
        result[0].someField == "aBc"
        cleanup:
        Test.executeUpdate('delete from Test')
    }

    void "test inList"() {
        setup:
        new Test(id: 0, someField: "abc", someField2: "abc").save()
        new Test(id: 1, someField: "aBc", someField2: "abc").save()
        new Test(id: 2, someField: "Bcd", someField2: "bcd").save()

        when:
        List result = builder.list() { inList('someField', ['abc', 'Bcd']) }

        then:
        result.size() == 2
        result[0].someField == "abc"
        result[1].someField == "Bcd"

        cleanup:
        Test.executeUpdate('delete from Test')
    }

    void "test nestedPathPropCall with a nested property"() {
        setup:
        new Test(id: 0, someField: '123', someField2: '123', nestedField: new Test2(someField: '456')).save()
        new Test(id: 0, someField: '321', someField2: '321', nestedField: new Test2(someField: '654')).save()

        when:
        List result = builder.list() { nestedPathPropCall('nestedField.someField', '456', 'eq') }

        then:
        result.size() == 1
        result[0].nestedField.someField == "456"
    }

}
