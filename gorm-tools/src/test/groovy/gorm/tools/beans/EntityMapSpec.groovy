/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans


import spock.lang.Specification

class EntityMapSpec extends Specification {

    // void testOverridePropertiesRecursionBug() {
    //     when:
    //     PropertyMapTest.metaClass.getProperties = {-> new EntityWrapperMap(delegate) }
    //
    //     def obj = new PropertyMapTest(name:"Homer", age:45)
    //
    //     then:
    //     !obj.properties.containsKey('properties')
    //     3 == obj.properties.size()
    // }

    void 'fo bar baz'() {

        when:
        def field = "foo.bar.baz"
        Integer nestedIndex = field.indexOf('.')
        String nestedProp = field.substring(0, nestedIndex)
        String theRest = field.substring(nestedIndex+1)

        then:
        'foo' == nestedProp
        'bar.baz' == theRest
    }

    void 'test default get includes'() {

        when:
        def tobj = new PogoBean(name:"Bart", age:11, other:"stuff")
        def map = new EntityMap(tobj)

        def includes = map.getIncludes()

        then:
        3 == map.size()
        3 == includes.size()
        ['name', 'age', 'other'].containsAll(includes)
    }

    // void testSelectSubMap() {
    //
    //     when:
    //     def map = new EntityWrapperMap(new PropertyMapTest(name:"Bart", age:11, other:"stuff"))
    //
    //     def submap = map['name', 'age']
    //
    //     then:
    //     2 == submap.size()
    //     "Bart" == submap.name
    //     11 == submap.age
    // }

    void testIsEmpty() {
        expect:
        def map = new EntityMap(new PogoBean())
        !map.isEmpty()
    }

    void testContainsKey() {
        expect:
        def map = new EntityMap(new PogoBean())
        map.containsKey("name")
        map.containsKey("age")
        !map.containsKey("fo")
    }

    void testContainsValue() {
        when:
        def map = new EntityMap(new PogoBean(name:"Homer", age:45))

        then:
        map == [name:"Homer", age:45, other: null]
        map.containsValue("Homer")
        map.containsValue(45)
        !map.containsValue("fo")
    }

    void testGet() {
        when:
        def map = new EntityMap(new PogoBean(name:"Homer", age:45))

        then:
        "Homer" == map.get("name")
        "Homer" == map.name
        "Homer" == map['name']

        45 == map.get("age")
        45 == map.age
        45 == map['age']

        map.foo == null
        map['foo'] == null
        map.get('foo') == null
    }

    void testPut() {
        def map = new EntityMap(new PogoBean(name:"Bart", age:11))

        map.name = "Homer"
        map.age = 45
        assertEquals "Homer", map.get("name")
        assertEquals "Homer", map.name
        assertEquals "Homer", map['name']

        def old = map.put("name", "lisa")
        assertEquals "Homer", old

        assertEquals "lisa", map.name
    }

    void testKeySet() {
        when:
        def map = new EntityMap(new PogoBean(name:"Bart", age:11))
        def keys = map.keySet()

        then:
        keys.contains("name")
        keys.contains("age")
    }

    void testValues() {
        when:
        def map = new EntityMap(new PogoBean(name:"Bart", age:11))
        def values = map.values()

        then:
        values.contains("Bart")
        values.contains(11)
    }

    void "test entrySet"() {
        when:
        def map = new EntityMap(new PogoBean(name:"Bart", age:11))
        def entset = map.entrySet()

        then:
        entset.size() == 3
        for(entry in map.entrySet()) {
            map.getIncludes().contains(entry.key)
        }

    }

    void "test nested bean"() {
        when:
        def map = new EntityMap(new PogoBean(name:"Bart", age:11))
        def entset = map.entrySet()

        then:
        entset.size() == 3
        for(entry in map.entrySet()) {
            map.getIncludes().contains(entry.key)
        }

    }
}

class PogoBean{
    String name
    Integer age
    String other
}
