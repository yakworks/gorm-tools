/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import gorm.tools.beans.map.MetaMap
import spock.lang.Specification

class EntityMapPogoSpec extends Specification {

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

    PogoBean pogoBean(){
        new PogoBean(
            name:"Bart", age: 45, other:"stuff",
            info: [ phone: "1234", email: "jo@jo.com" ],
            nested: new NestedBean( prop1: 'foo')
        )
    }

    void 'test default get includes'() {

        when:
        def map = new MetaMap(pogoBean())

        def includes = map.getIncludes()

        then:
        5 == map.size()
        5 == includes.size()
        ['name', 'age', 'other', 'info', 'nested'].containsAll(includes)
    }

    void testIsEmpty() {
        expect:
        def map = new MetaMap(new PogoBean())
        !map.isEmpty()
    }

    void testContainsKey() {
        expect:
        def map = new MetaMap(new PogoBean())
        map.containsKey("name")
        map.containsKey("age")
        !map.containsKey("fo")
    }

    void testContainsValue() {
        when:
        def map = new MetaMap(pogoBean())

        then:
        map.containsValue("Bart")
        map.containsValue(45)
        !map.containsValue("fo")
    }

    void testGet() {
        when:
        def map = new MetaMap(pogoBean())

        then:
        "Bart" == map.get("name")
        "Bart" == map.name
        "Bart" == map['name']

        45 == map.get("age")
        45 == map.age
        45 == map['age']

        map.foo == null
        map['foo'] == null
        map.get('foo') == null
    }

    void "put test"() {
        when:
        def map = new MetaMap(pogoBean())
        def old = map.put("name", "lisa")

        then:
        "Bart" == old
        "lisa" == map.name
    }

    void testKeySet() {
        when:
        def map = new MetaMap(pogoBean())
        def keys = map.keySet()

        then:
        keys.size() == 5
        keys.contains("name")
        keys.contains("age")
    }

    void testValues() {
        when:
        def map = new MetaMap(pogoBean())
        def values = map.values()

        then:
        values.contains("Bart")
        values.contains(45)
    }

    void "test entrySet"() {
        when:
        def map = new MetaMap(pogoBean())
        def entset = map.entrySet()

        then:
        entset.size() == 5
        for(entry in map.entrySet()) {
            map.getIncludes().contains(entry.key)
        }

    }

    void "test nested pogo"() {
        when:
        def map = new MetaMap(pogoBean())

        then:
        map.info instanceof MetaMap
        // pogos dont get wrapped unless they are refed in includes
        map.nested instanceof NestedBean
    }

}

class PogoBean {
    String name
    Integer age
    String other
    Map info
    NestedBean nested
}

class NestedBean {
    String prop1
}
