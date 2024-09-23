package yakworks.rally.orgs

import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.testing.gorm.TestDataJson
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class LocationSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = [Org, Contact, Location]

    Map buildMap(Map args) {
        args.org = build(Org) //TODO figure out why this is not being saved in build-test-data
        TestDataJson.buildMap(args, entityClass)
    }

    void "test addressHtml"() {
        setup:
        Location location = build(Location, city: 'Chicago', state: 'IL', country: 'US', save: false)
        location.street1 = street1
        location.street2 = street2
        location.zipCode = zipCode
        expect:
        location.addressHtml == addressHtml

        where:
        street1   | street2   | zipCode | addressHtml
        'street1' | 'street2' | 123     | "street1 <br/> street2 <br/> Chicago, IL 123"
        'street1' | 'street2' | ''      | "street1 <br/> street2 <br/> Chicago, IL"
        'street1' | ''        | ''      | "street1 <br/> Chicago, IL"
        ''        | 'street2' | 123     | "street2 <br/> Chicago, IL 123"
    }

    void "fail when no org"() {
        setup:
        Location location = build(Location, save: false)
        location.orgId = null

        when:
        boolean valid = location.validate()

        then:
        !valid
    }

}
