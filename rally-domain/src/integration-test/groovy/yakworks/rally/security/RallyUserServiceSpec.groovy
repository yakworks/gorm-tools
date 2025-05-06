package yakworks.rally.security


import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType
import yakworks.security.Roles
import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.model.SecRole
import yakworks.security.gorm.model.SecRoleUser
import yakworks.testing.gorm.integration.DomainIntTest

@Integration
@Rollback
class RallyUserServiceSpec extends Specification implements DomainIntTest {
    RallyUserService rallyUserService

    void setup() {
        authenticate(AppUser.get(1), 'MANAGER')
    }

    def sanityCheck(){
        expect:
        rallyUserService
    }

    void testGetOrgManagers() {
        when:
        Org branch = Org.of("T001", "Test 1", OrgType.Branch).persist()

        SecRole mgrRole = SecRole.create(code: 'MANAGER', name: Roles.MANAGER)
        SecRole colMgrRole = SecRole.create(code: 'AR_MANAGER', name: Roles.AR_MANAGER)
        SecRole collectorRole = SecRole.create(code: 'AR_COLLECTOR')

        assert mgrRole != null
        assert colMgrRole != null

        //user 1 with role manager
        Contact mgrContact1 = build(Contact, [email: 'manager1@foo.com', org: branch])
        rallyUserService.buildUserFromContact(mgrContact1, '1234')
        mgrContact1.persist(flush: true)
        SecRoleUser.create(mgrContact1.user, mgrRole)

        //user 2 with role AR_MANAGER
        Contact mgrContact2 = build(Contact, [email: 'manager2@foo.com', org: branch])
        rallyUserService.buildUserFromContact(mgrContact2, '1234')
        mgrContact2.persist(flush: true)
        SecRoleUser.create(mgrContact2.user, colMgrRole)

        // add a 3rd user without proper roles to ensure count
        Contact basicUser = build(Contact, [email: 'basicUser@foo.com', org: branch])
        rallyUserService.buildUserFromContact(basicUser, '1234')
        basicUser.persist(flush: true)
        SecRoleUser.create(basicUser.user, collectorRole)

        flush()

        then:
        // just a bunch of playground sanity check stuff
        def srlist = SecRoleUser.executeQuery("from SecRoleUser sru where sru.user.id = :uid", [uid: mgrContact1.id])
        srlist
        srlist.size() == 1

        def clist = Contact.executeQuery("from Contact con where con.org = :org", [org: branch])
        clist.size() == 3
        clist.each {
            assert it.user.id == it.id
        }

        List<AppUser> ulist = AppUser.executeQuery("""
            select user from Contact contact, AppUser user
            where contact.org = :org
            and user.id = contact.id
            and exists (
                from SecRoleUser sru where sru.user = user
            )
        """, [org: branch]) as List<AppUser>

        ulist.size() == 3
        ulist[0] instanceof AppUser

        when:
        def users = rallyUserService.getOrgManagers(branch.id)

        then:
        users.size() == 2
    }

}
