package yakworks.rally.orgs

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.rally.activity.ActivityCopier
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactEmail
import yakworks.rally.orgs.model.ContactPhone
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.seed.RallySeed
import yakworks.rally.testing.OrgDimensionTesting
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.unit.GormHibernateTest
import yakworks.testing.gorm.unit.SecurityTest

class OrgCopierSpec extends Specification implements GormHibernateTest, SecurityTest {
    static List entityClasses = RallySeed.entityClasses + [AttachmentLink]
    static List springBeans = RallySeed.springBeanList +  [ActivityCopier, OrgCopier]

    @Autowired OrgCopier orgCopier

    def "test copy"() {
        setup:
        OrgDimensionTesting.emptyDimensions()

        Org old = build(Org)
        // old.type = TestData.build(OrgType)
        // old.orgTypeId = old.type.id
        old.calc = build(OrgCalc, id: old.id)
        old.flex = build(OrgFlex, id: old.id)
        old.info = build(OrgInfo, id: old.id)
        old.member = RepoTestData.build(OrgMember, [id: old.id, branch: build(Org), division: build(Org)])

        Location location = RepoTestData.build(Location)
        old.location = location.persist()

        Contact contact = RepoTestData.build(Contact)
        //TestData.build(Location, [contact: contact])
        contact.addToPhones(RepoTestData.build(ContactPhone,[contact: contact]))
        contact.addToEmails(RepoTestData.build(ContactEmail,[contact: contact]))
        //location.contact = contact
        old.contact = contact
        //old.bind(calc: calc, flex: flex, location: location, member: member)
        old.persist(flush: true)

        expect:
        old.contact != null
        old.calc != null
        old.calcId == old.id
        old.calc.id == old.id
        old.flex != null
        old.member != null

        old.location != null
        old.contact != null

        when:
        Org copy = new Org()
        copy.bind(num: "n1")

        orgCopier.copy(old, copy)
        Org.repo.flush()

        then:
        copy.name == old.name
        copy.type == old.type
        copy.num != old.num //num should not be copied

        copy.calcId == copy.id
        copy.calc.curBal == old.calc.curBal
        copy.calc.pastDue == old.calc.pastDue

        copy.flex != null
        copy.flexId == copy.id
        copy.flex.date1 == old.flex.date1
        copy.flex.text1 == old.flex.text1
        copy.flex.text2 == old.flex.text2
        copy.flex.text3 == old.flex.text3

        copy.member.branch != null
        copy.member.branch == old.member.branch
        copy.member.division != null
        copy.member.division == old.member.division

        !copy.location.is(old.location) //should not be the same reference, should have been created new
        copy.location.org == copy
        copy.location.city == old.location.city
        copy.location.name == old.location.name

        copy.contact
        !copy.contact.is(old.contact)
        copy.contact.name == old.contact.name
    }
}
