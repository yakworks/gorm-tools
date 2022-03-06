package yakworks.rally.orgs

import spock.lang.IgnoreRest
import yakworks.gorm.testing.SecurityTest
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DataRepoTest
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType

class OrgSpec extends Specification implements DataRepoTest, SecurityTest {
    //Automatically runs the basic crud tests

    def setupSpec() {
        defineBeans {
            //scriptExecutorService(ScriptExecutorService)
            orgDimensionService(OrgDimensionService)
        }
        mockDomains(
            OrgTag, Location, Contact, Org, OrgSource, OrgFlex, OrgCalc, OrgInfo
        )
    }

    void "sanity check build"() {
        when:
        def org = build(Org)

        then:
        org.id

    }

    // void "CRUD tests"() {
    //     expect:
    //     createEntity().id
    //     persistEntity().id
    //     updateEntity().version > 0
    //     removeEntity()
    // }

    void "test org errors, no type"() {
        when:
        Org org = new Org(num:'foo1', name: "foo")

        then:
        !org.validate()
        org.errors.allErrors.size() == 1
        org.errors['type']
    }

    void "empty string for name or num"() {
        when:
        Org org = Org.of("", "", OrgType.Customer)

        then:
        !org.validate()
        org.errors['name'].code == 'NotBlank'
        org.errors['num'].code == 'NotBlank'
        org.errors.allErrors.size() == 2

    }

    def testOrgSourceChange() {
        when:
        Org org = Org.of("foo", "bar", OrgType.Customer)
        Org.repo.createSource(org)
        org.persist()
        assert org.source

        then: "source id is the default"
        assert org.source.sourceId == "foo"

        when: "sourceId is changed"
        org.source.sourceId = "test"
        org.source.persist()
        Long osi = org.source.id
        assert org.source.id

        then: "it should be good"
        assert org.source.sourceId == "test"
        assert OrgSource.get(osi).sourceId == "test"

        when: "flush and clear is called and OrgSource is retreived again"
        flushAndClear()

        then: "should stil be the sourceId that was set"
        assert OrgSource.get(osi).sourceId == "test"
    }

    def "create & update associations"() {
        setup:
        Long orgId = 1000

        Map flex = TestDataJson.buildMap(OrgFlex, includes: "*")
        Map calc = TestDataJson.buildMap(OrgCalc, includes: "*")
        Map info = TestDataJson.buildMap(OrgInfo, includes: "*")

        Map params = TestDataJson.buildMap(Org) << [id: orgId, flex: flex, info: info, type: 'Customer']

        when: "create"
        def org = Org.create(params, bindId: true)

        then:

        org.id == orgId
        org.flex.id
        org.info.id
        //entity.calc.id

        org.flex.text1
        org.info.phone
        org.info.fax
        org.info.website

        when: "update"
        org = Org.update([id: org.id, flex: [text1: 'yyy'], info: [phone: '555-1234', fax: '555-1234', website: 'www.test.com']])

        then:
        org.flex.text1 == 'yyy'
        org.info.phone == '555-1234'
        org.info.fax == '555-1234'
        org.info.website == 'www.test.com'
    }

    def "test insert with locations"() {
        setup:
        Long orgId = 10000
        //Map location = TestDataJson.buildMap(Location, includes:"*")
        List locations = [[street1: "street1"], [street1: "street loc2"]]
        Map params = TestDataJson.buildMap(Org) + [locations: locations]

        when:
        def org = Org.create(params)
        def locs = org.locations

        then:
        locs.size() == 2
        locs[0].street1 == locations[0].street1
        locs[1].street1 == locations[1].street1
        locs[0].org == org
    }

    void "test getOrgTypeFromData"() {
        expect:
        Org.repo.getOrgTypeFromData(data) == orgType

        where:

        orgType          | data
        OrgType.Customer | [orgTypeId: 1]
        OrgType.Branch   | [orgTypeId: '3']
        OrgType.Branch   | [type: OrgType.Branch]
        OrgType.Branch   | [type: [id: 3]]
    }

}
