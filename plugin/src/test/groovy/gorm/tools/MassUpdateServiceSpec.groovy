package gorm.tools

import gorm.tools.testing.unit.DataRepoTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import testing.Org
import testing.OrgType

class MassUpdateServiceSpec extends Specification implements DataRepoTest, ServiceUnitTest<MassUpdateService> {

    void setupSpec() {
        mockDomains(Org, OrgType)

    }

    void "test validate"() {
        given:
        Map safeChanges = [name: true, name2: true, type:[name:true]]
        Map changes = [
                name: "test",
                name2: "",
                type:[name: "test", extra:"foobar"]

        ]

        when:
        Map result = service.validate(changes, safeChanges)

        then:
        result.name == changes.name
        result.name2 == null //should have been set to null in result.
        result.type.name == changes.type.name
        result.type.extra == null //should have been not included in result
    }


    void "test mass update changes"() {
        given:
        Map safeChanges = [name: true, name2: true, inactive: true, type:[name:true]]
        Map changes = [
                name: "updated",
                name2: "updated",
                inactive: false,
                type:[name: "foobar", num:1]
        ]

        Closure getDomain = { key, domain ->
            if(key == "type") return new OrgType()
            return null
        }

        List<Org> orgs = (1..10).collect { it -> new Org(name: "name-$it", name2: "num-$it")}

        when:
        Map result = service.massUpdate(changes, orgs, safeChanges, "org", getDomain)

        then:
        noExceptionThrown()
        result.ok == true

        orgs.each { Org org ->
            assert org.name == changes.name
            assert org.name2 == changes.name2
            assert org.type != null
            assert org.type.name == changes.type.name
        }
    }

}
