/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import gorm.tools.utils.GormUtils
import grails.gorm.annotation.Entity
import grails.testing.gorm.DataTest
import spock.lang.Specification

class GormUtilsSpec extends Specification implements DataTest {

    void setupSpec() {
        //mockDomain Person
        mockDomains Person, Address
    }

    void "test copyDomain"() {
        setup:
        Address address = new Address(street: 'street1', city: 'city1').save()
        Person person = new Person(id: 0, version: 0, createdBy: 'test0', createdDate: '10-22-2017',
            editedBy: 'test0', editedDate: '10-22-2017', num: '000', name: 'Joey', age: 35, address: address).save()
        Person person2 = new Person(id: 1, version: 1, createdBy: 'test1', createdDate: '10-23-2017',
            editedBy: 'test1', editedDate: '10-23-2017', num: '111').save()

        when:
        Person copy = GormUtils.copyDomain(person2, person)

        then:
        copy.name == person.name
        copy.age == person.age
        copy.id != person.id
        copy.version != person.version
        copy.num != person.num
        copy.createdBy != person.createdBy
        copy.createdDate != person.createdDate
        copy.editedBy != person.editedBy
        copy.editedDate != person.editedDate
    }

    void "test copyDomain specifying a domain class as a target"() {
        setup:
        Address address = new Address(street: 'street1', city: 'city1').save()
        Person person = new Person(name: 'Joey', age: 35, address: address).save()

        when:
        Person copy = GormUtils.copyDomain(Person, person)

        then:
        copy.name == person.name
        copy.age == person.age
    }

    void "test copyProperties"() {
        setup:
        Address address = new Address(street: 'street', city: 'city1').save()
        Person person = new Person(name: 'Joey', age: 35, address: address).save()
        Person person2 = new Person(name: 'test', age: 0).save()
        Person person3 = new Person(name: 'test', age: 0).save()

        when:
        GormUtils.copyProperties(person, person2, false, 'name')
        GormUtils.copyProperties(person, person3, false, 'name', 'age', 'address')

        then:
        person2.name == person.name
        person2.age != person.age
        person3.name == person.name
        person3.age == person.age
        person3.address == person.address
    }

    void "test copyProperties, don't allow to copy if target's prop is not null"() {
        setup:
        Address address = new Address(street: 'street1', city: 'city1').save()
        Person person = new Person(name: 'Joey', age: 35, address: address).save()
        Person person2 = new Person(age: 25).save()

        when:
        GormUtils.copyProperties(person, person2, 'name', 'age', 'address')

        then:
        person2.name == person.name
        person2.age != person.age
        person2.address.street == person.address.street
        person2.address.city == person.address.city
    }

    void "test getPropertyValue"() {
        setup:
        Address address = new Address(street: 'street1', city: 'city1').save()
        Person person = new Person(name: 'Joey', age: 35, address: address).save()

        when:
        String name = GormUtils.getPropertyValue(person, 'name')
        int age = GormUtils.getPropertyValue(person, 'age')
        String street = GormUtils.getPropertyValue(person, 'address.street')

        then:
        name == person.name
        age == person.age
        street == address.street
    }

}

@Entity
class Person {
    String name
    int age
    String num
    Address address

    String createdBy
    String createdDate
    String editedBy
    String editedDate

    static constraints = {
        name blank: true, nullable: true
        age blank: true, nullable: true
        address nullable: true

        num blank: true, nullable: true
        createdBy blank: true, nullable: true
        createdDate blank: true, nullable: true
        editedBy blank: true, nullable: true
        editedDate blank: true, nullable: true
    }
}

@Entity
class Address {
    String street
    String city

    static constraints = {
        street blank: true
        city blank: true
    }
}
