package yakworks.security.rest

import gorm.tools.security.domain.AppUser
import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.testing.unit.DataRepoTest
import grails.plugin.springsecurity.rest.oauth.OauthUser
import grails.testing.services.ServiceUnitTest
import yakworks.security.rest.NineOauthUserDetailsService
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.pac4j.core.profile.CommonProfile
import org.springframework.security.core.userdetails.UserDetailsChecker
import org.springframework.security.core.userdetails.UserDetailsService
import spock.lang.Specification

class NineOauthUserDetailsServiceSpec extends Specification implements DataRepoTest, ServiceUnitTest<NineOauthUserDetailsService> {

    void setupSpec() {
        defineBeans({
            preAuthenticationChecks(InstanceFactoryBean, Mock(UserDetailsChecker), UserDetailsChecker)
            userDetailsService(InstanceFactoryBean, Mock(UserDetailsService), UserDetailsService)
        })
        mockDomains(AppUser, SecRole, SecRoleUser)
    }

    void "load user shoud not fail when no password"() {
        setup:
        AppUser appUser = build(AppUser, [username: "test", email: "test@9ci.com"])

        expect:
        AppUser.findByUsername("test")

        when:
        CommonProfile profile = Mock(CommonProfile) {
            getId() >> "test"
        }

        OauthUser oauthUser = service.loadUser(profile, [])

        then:
        oauthUser != null
        oauthUser.password == "N/A"
        oauthUser.username == "test"
    }
}
