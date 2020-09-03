package gorm.tools.security

import gorm.tools.security.RallyUserDetailsService
import gorm.tools.security.domain.SecUser
import gorm.tools.testing.integration.DataIntegrationTest
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.testing.mixin.integration.Integration
import grails.transaction.Rollback
import spock.lang.Specification

@Integration
@Rollback
class RallyUserDetailsServiceSpec extends Specification implements DataIntegrationTest {
    RallyUserDetailsService userDetailsService

    void testLoadUserByUsername() {
        when:
        SecUser.repo.create([name:"Karen", login:"karen", password:"karen", repassword:"karen", email:"karen@9ci.com"])
        GrailsUser gUser = userDetailsService.loadUserByUsername('karen')

        then:
        gUser != null

        when:
        SecUser user = SecUser.get(gUser.id)

        then:
        user.name== 'Karen'
    }

    void "test expired password"() {
        given:
        Date now = new Date()
        SecUser user = SecUser.first()
        userDetailsService.passwordExpireEnabled = true
        userDetailsService.passwordExpireDays = 10
        user.mustChangePassword = true
        user.passwordChangedDate = now - 11
        user.save()

        when:
        GrailsUser nineUser = userDetailsService.loadUserByUsername(user.login, false)

        then:
        nineUser.credentialsNonExpired == false

        cleanup:
        userDetailsService.passwordExpireEnabled = false
        userDetailsService.passwordExpireDays = 30
    }

}
