package gorm.tools.security

import org.apache.commons.lang3.RandomStringUtils

import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.testing.SecurityTest
import gorm.tools.testing.TestDataJson
import gorm.tools.testing.unit.DataRepoTest
import gorm.tools.testing.unit.DomainRepoTest
import spock.lang.Specification

class SecRoleSpec extends Specification implements DataRepoTest, SecurityTest {

    void setupSpec() {
        mockDomains AppUser, SecRole, SecRoleUser
    }

    String genRandomEmail(){
        String ename = RandomStringUtils.randomAlphabetic(10)
        return "${ename}@baz.com"
    }

    AppUser createUser(){
        def entity = new AppUser(
            username: 'billy',
            email: genRandomEmail(),
            password: 'test_pass_123'
        )
        return entity.persist(flush: true)
    }

    SecRole createRole(String name){
        def entity = new SecRole(name: name)
        return entity.persist(flush: true)
    }

    void "secRoleUser create"() {
        when:
        def user = createUser()
        def role = createRole('admin')
        def sru = SecRoleUser.create(user, role, true)

        then:
        sru.user == user
        sru.role == role
    }

    void "secRoleUser repo create"() {
        when:
        def user = createUser()
        def role = createRole('admin')
        def sru = SecRoleUser.create([userId: user.id, roleId: role.id])

        then:
        sru.user == user
        sru.role == role
    }

    void "secRoleUser repo create with objects"() {
        when:
        def user = createUser()
        def role = createRole('admin')
        def sru = SecRoleUser.create([user:[id: user.id], role:[id: role.id]])

        then:
        sru.user == user
        sru.role == role
    }

}
