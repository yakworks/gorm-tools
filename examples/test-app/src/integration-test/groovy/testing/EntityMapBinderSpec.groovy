package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import repoapp.Address
import repoapp.Org
import repoapp.Person
import spock.lang.Issue
import spock.lang.Specification

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {

    @Issue("https://github.com/yakworks/gorm-tools/issues/181")
    void "perform gormtools binding after grails binding"() {
        setup:
        Map params = [name:"test-org", address:[city:"Rajkot", testId:"100"]]

        when: "Address is bound as part of org association binding"
        Org org = new Org()
        org.properties = params

        then:
        org.hasErrors() == false
        org.name == "test-org"
        org.address != null
        org.address.city == "Rajkot"

        when: "now try to bind just address"
        Address address = new Address()
        address.bind params.address

        then:
        address != null
        address.city == "Rajkot"
    }

    void "test bindable : should create new instance"() {
        given:
        Map params = [name: "test", age: "50", address: [city: "Delhi", testId: "1"]]

        when:
        Person p = new Person()
        p.bind params

        then:
        p.name == "test"
        p.age == 50
        p.address != null
        p.address.city == "Delhi"
        p.address.testId == 1
    }

    void "should update existing associated instance when bindable"() {
        given:
        Address address = new Address(city: "Delhi", testId: 1)
        address.save()

        Person p = new Person(name: "test", age: 50, address: address)
        p.save()

        when:
        p = Person.get(p.id)

        then:
        p != null

        when:
        Map params = [address: [id:address.id, city: "Nyc", testId: "2"]]
        p.bind params

        then:
        p.address.id == address.id
        p.address.city == "Nyc"
        p.address.testId == 2
    }
}
