package yakworks.rally.orgs

import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification
import yakworks.rally.config.OrgProps
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.model.OrgSource
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.orgs.model.OrgType
import yakworks.testing.gorm.TestDataJson
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class OrgSpec extends Specification implements GormHibernateTest, SecurityTest {

    static entityClasses = [Org, OrgSource, OrgTag, Location, Contact, OrgFlex, OrgCalc, OrgInfo, OrgMember]

    Closure doWithGormBeans(){ { ->
        orgDimensionService(OrgDimensionService)
        orgProps(OrgProps)
    }}

    void "sanity check build"() {
        when:
        def org = build(Org)

        then:
        org.id

    }

    void "test create with of"() {
        when:
        Org org = Org.of('foo1', "foo", OrgType.Division)
        org.persist(flush: true)

        then:
        org.companyId == 2
    }


    void "test org errors, no type"() {
        when:
        Org org = new Org(num:'foo1', name: "foo")

        then:
        !org.validate()
        org.errors.allErrors.size() == 1
        org.errors['type']
    }

    void "test org errors long name"() {
        when:
        Org org = new Org(type: OrgType.Customer, num:'foo1',
            name: RandomStringUtils.randomAlphabetic(300),
            comments: RandomStringUtils.randomAlphabetic(300),
        )

        then:
        !org.validate()
        org.errors.allErrors.size() == 2
        org.errors['name'].code == 'MaxLength'
        org.errors['comments'].code == 'MaxLength'
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

    void testOrgSourceChange() {
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

    void "create & update associations"() {
        setup:
        Long orgId = 1000

        Map flex = TestDataJson.buildMap(OrgFlex, includes: "*", save:false)
        Map calc = TestDataJson.buildMap(OrgCalc, includes: "*", save:false)
        Map info = TestDataJson.buildMap(OrgInfo, includes: "*", save:false)

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

    void "test insert with locations"() {
        setup:
        Long orgId = 10000
        //Map location = TestDataJson.buildMap(Location, includes:"*")
        List locations = [[street1: "street1"], [street1: "street loc2"]]
        Map params = TestDataJson.buildMap(Org, save:false) + [locations: locations]

        when:
        def org = Org.create(params)
        flush()

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
