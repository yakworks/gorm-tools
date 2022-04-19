package nine.security

import org.springframework.security.crypto.password.PasswordEncoder

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.Rollback
import nine.rally.testing.DomainIntTest
import spock.lang.Specification
import gorm.tools.security.services.SecService
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.domain.AppUser
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class RallyUserServiceSpec extends Specification implements DomainIntTest {
    RallyUserService rallyUserService
    //UserRepo userRepo
    SecService secService
    PasswordEncoder passwordEncoder

    static String CONTACT_PASSWORD = "password"

    void setup() {
        authenticate(AppUser.get(50), Roles.MANAGER)
        // ['T001', 'T002', 'T003', 'T004'].each { String num ->
        //     Org.findByNum(num)?.delete(flush: true)
        //     Contact.findByFirstName(num)?.delete(flush: true)
        //     AppUser user = AppUser.findByUsername(num)
        //     if (user) {
        //         SecRoleUser.findAllByUser(user).each { it.delete(flush: true) }
        //         user.delete(flush: true)
        //     }
        // }

    }

    void testGetOrgManagers() {
        when:
        Org branch = Org.of("T001", "Test 1", OrgType.Branch).persist()

        SecRole mgrRole = SecRole.findByName(Roles.MANAGER)
        SecRole colMgrRole = SecRole.findByName(Roles.COLLECTIONS_MANAGER)

        assert mgrRole != null
        assert colMgrRole != null

        Contact mgrContact1 = build(Contact, [email: 'manager1@foo.com', org: branch])
        rallyUserService.buildUserFromContact(mgrContact1, '1234')
        mgrContact1.persist(flush: true)
        SecRoleUser.create(mgrContact1.user, mgrRole)

        Contact mgrContact2 = build(Contact, [email: 'manager2@foo.com', org: branch])
        rallyUserService.buildUserFromContact(mgrContact2, '1234')
        mgrContact2.persist(flush: true)
        SecRoleUser.create(mgrContact2.user, colMgrRole)

        // add a 3rd user without proper roles to ensure count
        Contact basicUser = build(Contact, [email: 'basicUser@foo.com', org: branch])
        rallyUserService.buildUserFromContact(basicUser, '1234')
        basicUser.persist(flush: true)
        SecRoleUser.create(basicUser.user, SecRole.findByName(Roles.COLLECTIONS))

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
        //clist[0].user instanceof AppUser

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
