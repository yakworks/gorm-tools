/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import spock.lang.IgnoreRest
import spock.lang.Specification

class EntityMapMapSpec extends Specification {

    Map testMap(){
        return [name:"Bart", age:45, other:"stuff", info: [ phone: "1234", email: "jo@jo.com" ]]
    }

    void 'test default get includes'() {

        when:
        Map tobj = testMap()
        def map = new EntityMap(tobj)

        def includes = map.getIncludes()

        then:
        4 == map.size()
        4 == includes.size()
        ['name', 'age', 'other', 'info'].containsAll(includes)
    }

    void testSelectSubMap() {

        when:
        def map = new EntityMap(testMap())

        def submap = map['name', 'age']

        then:
        2 == submap.size()
        "Bart" == submap.name
        45 == submap.age
    }

    void testIsEmpty() {
        expect:
        def map = new EntityMap(testMap())
        !map.isEmpty()

        def emptyMap = [:]
        emptyMap.isEmpty()
        def mapEmpty = new EntityMap(emptyMap)
        mapEmpty.isEmpty()
    }

    void testContainsKey() {
        expect:
        def map = new EntityMap(testMap())
        map.containsKey("name")
        map.containsKey("age")
        !map.containsKey("fo")
    }

    void testContainsValue() {
        when:
        def map = new EntityMap([name:"Homer", age:45])

        then:
        map == [name:"Homer", age:45]
        map.containsValue("Homer")
        map.containsValue(45)
        !map.containsValue("fo")
    }

    void testGet() {
        when:
        def map = new EntityMap(testMap())

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

    void "get nested"() {
        when:
        def map = new EntityMap(testMap())

        then:
        map.info instanceof EntityMap

    }

    void "put test"() {
        when:
        def map = new EntityMap(testMap())
        def old = map.put("name", "lisa")

        then:
        "Bart" == old
        "lisa" == map.name
    }

    void testKeySet() {
        when:
        def map = new EntityMap(testMap())
        def keys = map.keySet()

        then:
        keys.size() == 4
        keys.contains("name")
        keys.contains("age")
    }

    void testValues() {
        when:
        def map = new EntityMap(testMap())
        def values = map.values()

        then:
        values.contains("Bart")
        values.contains(45)
    }

    void "test entrySet"() {
        when:
        def map = new EntityMap(testMap())
        def entset = map.entrySet()

        then:
        entset.size() == 4
        for(entry in map.entrySet()) {
            map.getIncludes().contains(entry.key)
        }

    }

}
