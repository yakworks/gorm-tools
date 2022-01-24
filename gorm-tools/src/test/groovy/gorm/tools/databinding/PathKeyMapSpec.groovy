package gorm.tools.databinding

import spock.lang.Specification

class PathKeyMapSpec extends Specification {

    PathKeyMap theMap

    PathKeyMap getSample(){
        Map sub = [:]
        sub.put("name", "Dierk Koenig")
        sub.put("dob", "01/01/1970")
        sub.put("address.postCode", "345435")
        sub.put("address.town", "Swindon")
        sub.put("nested", PathKeyMap.of(['foo.bar': 'baz']))

        List nestedList = []
        nestedList << PathKeyMap.of(['foo.bar': 'baz'])
        nestedList << PathKeyMap.of(['foo.bar': 'baz'])
        sub.put("nestedList", nestedList)

        return PathKeyMap.of(sub)
    }

    void "test variations"() {

        when:
        PathKeyMap theMap = getSample().init()

        then:
        theMap['name', 'dob'] == [name:"Dierk Koenig", dob:"01/01/1970"]
        theMap.address.postCode == "345435"
        theMap.address.town == "Swindon"
        theMap.nested.foo.bar == 'baz'
        theMap.nestedList[0].foo.bar == 'baz'
        theMap.nestedList[1].foo.bar == 'baz'
    }

    void "test cloneMap"() {

        when:
        PathKeyMap initial = getSample()
        PathKeyMap theMap = initial.cloneMap().init()

        then:
        theMap['name', 'dob'] == [name:"Dierk Koenig", dob:"01/01/1970"]
        theMap.address.postCode == "345435"
        theMap.address.town == "Swindon"
        theMap.nested.foo.bar == 'baz'
        theMap.nestedList[0].foo.bar == 'baz'
        theMap.nestedList[1].foo.bar == 'baz'
    }

    void testMultiDimensionParams() {
        given:
        Map sub = [
            "a.b.c": "cValue",
            "a.b": "bValue",
            "a.bc": "bcValue",
            "a.b.d": "dValue",
            "a.e.f": "fValue",
            "a.e.g": "gValue",
            "x.y" : "yValue"
        ]

        when:
        theMap = PathKeyMap.create(sub)

        then:
        assert theMap['a'] instanceof Map
        assert theMap.a.b == "bValue"
        assert theMap["a.b.c"] == "cValue"
        assert theMap.a.'b.c' == "cValue"
        assert theMap.a.'bc' == "bcValue"
        assert theMap.a.'b.d' == "dValue"

        assert theMap.a['e'] instanceof Map
        assert theMap.a.e.f == "fValue"
        assert theMap.a.e.g == "gValue"
        assert theMap.x.y == "yValue"
        assert theMap["x.y"] == "yValue"
    }

    void "test multi dimensional map with different delim"() {
        given:
        Map sub = [
            "a_b_c": "cValue",
            "a_b": "bValue",
            "a_bc": "bcValue",
            "a_b_d": "dValue",
            "a_e_f": "fValue",
            "a_e_g": "gValue"
        ]

        when:
        theMap = PathKeyMap.of(sub, "_").init()

        then:
        assert theMap['a'] instanceof Map
        assert theMap.a.b == "bValue"
        assert theMap.a."b_c" == "cValue"
        assert theMap.a.'bc' == "bcValue"
        assert theMap.a."b_d" == "dValue"

        assert theMap.a['e'] instanceof Map
        assert theMap.a.e.f == "fValue"
        assert theMap.a.e.g == "gValue"
    }


    void testPlusOperator() {
        given:
        Map m = ["album": "Foxtrot"]
        def originalMap =  PathKeyMap.create(m)

        when:
        def newMap = originalMap + [vocalist: 'Peter']

        then:
        originalMap.containsKey('album')
        !originalMap.containsKey('vocalist')
        newMap.containsKey('album')
        newMap.containsKey('vocalist')
    }

    void testConversionHelperMethods() {
        given:
        def map = PathKeyMap.create([:])

        when:
        map.one = "1"
        map.aList = [1,2]
        map.array = ["one", "two" ] as String[]


        then:
        ["1"] ==  map.list("one")
        [1,2] == map.list("aList")
        ["one","two"] == map.list("array")
        [] == map.list("nonexistant")

    }

    void testNestedKeyAutoGeneration() {
        given:
        def params = PathKeyMap.of([:])

        when:
        params.'company.department.team.numberOfEmployees' = 42
        params.'company.department.numberOfEmployees' = 2112
        def firstKey = 'alpha'
        def secondKey = 'beta'
        params."${firstKey}.${secondKey}.foo" = 'omega'
        params.put "prefix.${firstKey}.${secondKey}", 'delta'

        def company = params.company

        then:
        assert company instanceof Map

        when:
        def department = company.department

        then:
        assert department instanceof Map
        assert department.numberOfEmployees == 2112

        when:
        def team = department.team

        then:
        assert team instanceof Map
        assert team.numberOfEmployees == 42

        assert params['alpha'] instanceof Map
        assert params['alpha']['beta'] instanceof Map
        assert params['alpha']['beta'].foo == 'omega'

        assert params['prefix'] instanceof Map
        assert params['prefix']['alpha'] instanceof Map
        assert params['prefix']['alpha'].beta == 'delta'
    }


    void testCloning() {
        Map sub = ["name":"Dierk Koenig", "address.postCode": "345435", "dob": "01/01/1970"]
        theMap =  PathKeyMap.of(sub)

        when:
        Map theClone = theMap.clone()

        then:
        theMap.size() == theClone.size()
        theMap.each { k, v ->
            assert theMap[k] == theClone[k], "theclone should have the same value for $k as the original"
        }
    }

    void "list of PathKeyMaps in "() {
        Map sub = ["name":"Dierk Koenig", "address.postCode": "345435", "dob": "01/01/1970"]
        theMap =  PathKeyMap.of(sub)

        when:
        Map theClone = theMap.clone()

        then:
        theMap.size() == theClone.size()
        theMap.each { k, v ->
            assert theMap[k] == theClone[k], "theclone should have the same value for $k as the original"
        }
    }

}
