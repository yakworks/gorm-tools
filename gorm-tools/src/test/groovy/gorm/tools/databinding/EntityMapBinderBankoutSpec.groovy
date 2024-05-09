package gorm.tools.databinding

import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.unit.DataRepoTest

import java.time.LocalDate

class EntityMapBinderBankoutSpec extends Specification implements DataRepoTest  {

    EntityMapBinder binder

    Class[] getDomainClassesToMock() {
        [TestDomain, Nest, AnotherDomain, BindableNested, KitchenSink]
    }

    void setup() {
        binder = new EntityMapBinder()
    }

    void "test binder should convert empty values to null"() {
        given:
        TestDomain testDomain = new TestDomain()
        Map params = [name: "  ", age: "", amount: "", localDate: "", active: "  "]

        when:
        binder.bind(testDomain, params)

        then: "No exceptions or class cast errors should have been generates, empty values set as null"
        noExceptionThrown()

        testDomain.hasErrors() == false
        testDomain.name == null
        testDomain.age == null
        testDomain.amount == null
        testDomain.localDate == null
        testDomain.active == null
    }

    void "bind update : blank out values with empty strings"() {
        given:
        TestDomain testDomain = testDomainInstance()

        when: "empty strings"
        Map params = [
            name:"",
            age:"",
            amount: "",
            active: "",
            localDate: "",
            testEnum: "",
            anotherDomain: null //empty values  doesnt set an association to null
        ]

        binder.bind(testDomain, params)

        then:
        noExceptionThrown()
        verifyAllNull(testDomain)
    }

    void "bind update : blank out values with null"() {
        given:
        TestDomain testDomain = testDomainInstance()

        when: "empty strings"
        Map params = [
            name:null,
            age:null,
            amount: null,
            active: null,
            localDate: null,
            testEnum: null,
            anotherDomain: null
        ]

        binder.bind(testDomain, params)

        then:
        noExceptionThrown()
        verifyAllNull(testDomain)
    }

    void verifyAllNull(def object) {

        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(object.getClass() as Class)
        PersistentEntity entity = gormStaticApi.gormPersistentEntity
        List<String> properties = entity.persistentPropertyNames

        properties.each {
            assert object[it] == null
        }
    }

    TestDomain testDomainInstance() {
        AnotherDomain nested = new AnotherDomain(name:"test")
        TestDomain testDomain = new TestDomain(name:"test", age:40, amount: 100.00, active:true, localDate: LocalDate.now(), testEnum:"FOO")
        testDomain.anotherDomain = nested

        return testDomain
    }

}
