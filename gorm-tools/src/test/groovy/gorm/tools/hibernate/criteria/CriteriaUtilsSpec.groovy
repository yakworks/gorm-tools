/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.criteria

import gorm.tools.testing.hibernate.GormToolsHibernateSpec
import grails.persistence.Entity

class CriteriaUtilsSpec extends GormToolsHibernateSpec {

    List<Class> getDomainClasses() { [Test, Test2] }

    def setup() {
        (1..3).each { index ->
            String value = "someField_" + index
            new Test(id: index,
                someField: value,
                someField2: value,
                nestedField: new Test2(someField: value)
            ).save()
        }
    }

    def "test order for one column"() {
        when:
        List res = Test.createCriteria().list() {
            CriteriaUtils.applyOrder([sort: "someField", order: "desc"], delegate)
        }

        List res2 = Test.createCriteria().list() {
            order("someField", "desc")
        }

        then:
        res.eachWithIndex { Test entry, int i ->
            assert entry.someField == res2[i].someField
        }
    }

    def "test order for two columns"() {
        when:
        List res = Test.createCriteria().list() {
            CriteriaUtils.applyOrder([sort: "someField asc, someField", order: "desc"], delegate)
        }

        List res2 = Test.createCriteria().list() {
            and {
                order("someField", "asc")
                order("someField2", "desc")
            }
        }

        then:
        res.eachWithIndex { Test entry, int i ->
            assert entry.someField == res2[i].someField
            assert entry.someField2 == res2[i].someField2
        }
    }

    def "test order with nested field"() {
        when:
        List res = Test.createCriteria().list() {
            CriteriaUtils.applyOrder([sort: "nestedField.id asc, someField", order: "desc"], delegate)
        }

        List res2 = Test.createCriteria().list() {
            and {
                nestedField {
                    order("id", "asc")
                }
                order("someField", "desc")
            }
        }

        then:
        res.eachWithIndex { def entry, int i ->
            assert entry.someField == res2[i].someField
            assert entry.someField2 == res2[i].someField2
            assert entry.nestedField.id == res2[i].nestedField.id
        }
    }

    def "test order with join"() {
        when:
        List res = Test.createCriteria().list() {
            CriteriaUtils.applyOrder([sort: "nestedField.id", order: "desc"], delegate, ["nestedField.id"])
        }

        then:
        assert res.size() == Test.list().size()
    }

}

@Entity
class Test {
    String someField
    String someField2
    Test2 nestedField

    static constraints = {
        someField blank: false
        someField2 blank: false
        nestedField blank: true, nullable: true
    }
}

@Entity
class Test2 {
    String someField

    static constraints = {
        someField blank: false
    }
}
