/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.criteria

import spock.lang.Specification
import yakworks.testing.gorm.unit.GormHibernateTest

class GormHibernateCriteriaBuilderSpec extends Specification implements GormHibernateTest {

    static List entityClasses = [Test]

    private GormHibernateCriteriaBuilder builder

    void setup() {
        assert sessionFactory
        builder = new GormHibernateCriteriaBuilder(Test, sessionFactory)
    }

    void "test order"() {
        setup:
        (1..3).each { index ->
            new Test(someField: "aaa_$index", someField2: "bbb_$index").save()
        }
        flush()

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
            new Test(
                someField: "1", someField2: "2", nestedField: new Test2(someField: "abc_$index").save()
            ).save()
        }
        flush()

        when:
        List result = builder.list() { order("nestedField.someField", "desc") }

        then:
        result[0].nestedField.someField == "abc_2"
        result[1].nestedField.someField == "abc_1"
        result[2].nestedField.someField == "abc_0"
    }

    void "test ilike"() {
        setup:
        new Test(someField: "abc", someField2: "abc").save()
        new Test(someField: "aBc", someField2: "abc").save()
        new Test(someField: "Bcd", someField2: "bcd").save()
        flush()

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
        new Test(someField: "abc", someField2: "abc").save()
        new Test(someField: "aBc", someField2: "abc").save()
        new Test(someField: "Bcd", someField2: "bcd").save()
        flush()

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
        new Test(someField: "abc", someField2: "abc").save()
        new Test(someField: "aBc", someField2: "abc").save()
        new Test(someField: "Bcd", someField2: "bcd").save()
        flush()

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
        flush()

        when:
        List result = builder.list() { nestedPathPropCall('nestedField.someField', '456', 'eq') }

        then:
        result.size() == 1
        result[0].nestedField.someField == "456"
    }

}
