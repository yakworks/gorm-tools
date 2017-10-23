package gorm.tools

import grails.test.mixin.TestMixin
import grails.test.mixin.gorm.Domain
import grails.test.mixin.hibernate.HibernateTestMixin
import spock.lang.Specification
import grails.gorm.annotation.Entity

@Domain([Person, Address])
@TestMixin(HibernateTestMixin)
class GormUtilsSpec extends Specification {

    void "test copyDomain"() {
        setup:
        Address address = new Address(street: 'street', city: 'city1').save()
        Person person = new Person(name: 'Joey', age: 35, address: address).save()
        Person person2 = new Person().save()

        when:
        Person copy = GormUtils.copyDomain(person2, person)

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

    List<Class> getDomainClasses() {
        return [Person]
    }
}

@Entity
class Person {
    String name
    int age
    Address address

    static constraints = {
        name blank: true, nullable: true
        age blank: true, nullable: true
        address nullable: true
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
