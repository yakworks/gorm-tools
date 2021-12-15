package gorm.tools.databinding

import spock.lang.Specification

class PathKeyMapTests extends Specification {

    PathKeyMap theMap

    void testSimple() {
        given:
        Map sub = [:]
        sub.put("name", "Dierk Koenig")
        sub.put("dob", "01/01/1970")
        sub.put("address.postCode", "345435")
        sub.put("address.town", "Swindon")

        when:
        theMap = new PathKeyMap(sub)

        then:
        theMap['name', 'dob'] == [name:"Dierk Koenig", dob:"01/01/1970"]
        theMap.address.postCode == "345435"
        theMap.address.town == "Swindon"
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
        theMap = new PathKeyMap(sub)

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
        assert theMap["x.y"] == "vValue"
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
        theMap = new PathKeyMap(sub, "_")

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


    void "date tests"() {
        when: "single format"
        def params = new PathKeyMap([:])
        params['myDate'] = '16-07-1971'
        def val = params.date('myDate', 'dd-MM-yyyy')
        def cal = new GregorianCalendar(1971,6,16)

        then:
        val == cal.time

        when: "multi format"
        params = new PathKeyMap([:])
        params['myDate'] = '710716'
        val = params.date('myDate', ['yyyy-MM-dd', 'yyyyMMdd', 'yyMMdd'])
        cal = new GregorianCalendar(1971,6,16)

        then:
        assert val == cal.time
    }

    void testPlusOperator() {
        given:
        Map m = ["album": "Foxtrot"]
        def originalMap = new PathKeyMap(m)

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
        def map = new PathKeyMap([:])

        when:
        map.zero = "0"
        map.one = "1"
        map.bad = "foo"
        map.decimals = "1.4"
        map.bool = "true"
        map.aList = [1,2]
        map.array = ["one", "two" ] as String[]
        map.longNumber = 1234567890
        map.z = 'z'

        then:
        ["1"] ==  map.list("one")
        [1,2] == map.list("aList")
        ["one","two"] == map.list("array")
        [] == map.list("nonexistant")
        1 == map.byte('one')
        -46 == map.byte('longNumber') // overflows
        map.byte("test") == null
        map.byte("bad") == null
        map.byte("nonexistant") == null
        0 == map.byte('zero')
        1 == map.byte('one', 42 as Byte)
        0 == map.byte('zero', 42 as Byte)
        42 == map.byte('bad', 42 as Byte)
        42 == map.byte('nonexistent', 42 as Byte)
        1 == map.byte('one', 42)
        0 == map.byte('zero', 42)
        42 == map.byte('bad', 42)
        42 == map.byte('nonexistent', 42)
        '1' == map.char('one')
        map.char('longNumber') == null
        map.char("test") == null
        map.char("bad") == null
        map.char("nonexistant") == null
        '0' == map.char('zero')
        '1' == map.char('one', 'A' as Character)
        '0' == map.char('zero', 'A' as Character)
        'A' == map.char('bad', 'A' as Character)
        'A' == map.char('nonexistent', 'A' as Character)
        '1' == map.char('one', (char)'A')
        '0' == map.char('zero', (char)'A')
        'A' == map.char('bad', (char)'A')
        'A' == map.char('nonexistent', (char)'A')
        'z' == map.char('z')
        'z' == map.char('z', (char)'A')
        'z' == map.char('z', 'A' as Character)

         1 == map.int('one')
        map.int("test") == null
        map.int("bad") == null
        map.int("nonexistant") == null
        0 == map.int('zero')
        1 == map.int('one', 42)
        0 == map.int('zero', 42)
        42 == map.int('bad', 42)
        42 == map.int('nonexistent', 42)

        1L == map.long('one')
        map.long("test") == null
        map.long("bad") == null
        map.long("nonexistant") == null
        0L == map.long('zero')
        1L == map.long('one', 42L)
        0L == map.long('zero', 42L)
        42L == map.long('bad', 42L)
        42L == map.long('nonexistent', 42L)

        1 == map.short('one')
        map.short("test") == null
        map.short("bad") == null
        map.short("nonexistant") == null
        0 == map.short('zero')
        1 == map.short('one', 42 as Short)
        0 == map.short('zero', 42 as Short)
        42 == map.short('bad', 42 as Short)
        42 == map.short('nonexistent', 42 as Short)
        1 == map.short('one', 42)
        0 == map.short('zero', 42)
        42 == map.short('bad', 42)
        42 == map.short('nonexistent', 42)

        1.0 == map.double('one')
        1.4 == map.double('decimals')
        map.double("bad") == null
        map.double("nonexistant") == null
        0.0 == map.double('zero')
        1.0 == map.double('one', 42.0)
        0.0 == map.double('zero', 42.0)
        42.0 == map.double('bad', 42.0)
        42.0 == map.double('nonexistent', 42.0)

        1.0 == map.float('one')
        1.399999976158142 == map.float('decimals')
        map.float("bad") == null
        map.float("nonexistant") == null
        0.0f == map.float('zero')
        1.0f == map.float('one', 42.0f)
        0.0f ==  map.float('zero', 42.0f)
        42.0f == map.float('bad', 42.0f)
        42.0f == map.float('nonexistent', 42.0f)

        map.boolean('one') == true
        map.boolean('nonexistent', Boolean.TRUE) == true
        map.boolean('nonexistent', Boolean.FALSE) == false
        map.boolean('bool') == true
        map.boolean("nonexistant") == null
        map.boolean('my_checkbox') == null

        when:
        map.my_checkbox = false

        then:
        !map.boolean('my_checkbox')

        when:
        map.my_checkbox = true

        then:
        map.boolean('my_checkbox')

        when:
        map.my_checkbox = 'false'

        then:
        !map.boolean('my_checkbox')

        when:
        map.my_checkbox = 'true'

        then:
        map.boolean('my_checkbox')

        when:
        map.my_checkbox = 'some bogus value'

        then:
        !map.boolean('my_checkbox')

        when:
        map.my_checkbox = 'off'

        then:
        !map.boolean('my_checkbox')

        when:
        map.my_checkbox = 'on'

        then:
        map.boolean('my_checkbox')
    }

    void testNestedKeyAutoGeneration() {
        given:
        def params = new PathKeyMap([:])

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
        theMap = new PathKeyMap(sub)

        when:
        Map theClone = theMap.clone()

        then:
        theMap.size() == theClone.size()
        theMap.each { k, v ->
            assert theMap[k] == theClone[k], "theclone should have the same value for $k as the original"
        }
    }

}
