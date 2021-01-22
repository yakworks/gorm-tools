package testing

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Issue
import spock.lang.Specification
import yakworks.taskify.domain.Contact
import yakworks.taskify.domain.ContactAddress
import yakworks.taskify.repo.ContactRepo
import yakworks.taskify.domain.Location
import yakworks.taskify.domain.Org

@Integration
@Rollback
class EntityMapBinderSpec extends Specification {

    @Issue("https://github.com/yakworks/gorm-tools/issues/181")
    void "perform gormtools binding after grails binding"() {
        setup:
        Map params = [name:"test-org", location:[city:"Rajkot"]]

        when: "Address is bound as part of org association binding"
        Org org = new Org()
        org.properties = params

        then:
        org.hasErrors() == false
        org.name == "test-org"
        org.location != null
        org.location.city == "Rajkot"

        when: "now try to bind just address"
        Location address = new Location()
        address.bind params.location

        then:
        address != null
        address.city == "Rajkot"
    }

    void "test bindable : should create new instance"() {
        given:
        Map params = [firstName: "bill", age: "50", address: [street:"123", city: "Delhi"]]

        when:
        Contact p = new Contact()
        p instanceof ContactRepo
        p.bind params

        then:
        p.firstName == "bill"
        p.age == 50
        p.address != null
        p.address.city == "Delhi"
    }

    void "should update existing associated instance when bindable"() {
        given:
        ContactAddress address = new ContactAddress(city: "Delhi", street:"123")
        address.persist()

        Contact p = new Contact(firstName: "test", age: 50, address: address)
        p.persist()

        when:
        p = Contact.get(p.id)

        then:
        p != null

        when:
        Map params = [address: [id:address.id, city: "Nyc", street:"123"]]
        p.bind params

        then:
        p.address.id == address.id
        p.address.city == "Nyc"
        p.address.street == '123'
    }
}
