package yakworks.security.spring


import org.springframework.security.saml2.provider.service.authentication.DefaultSaml2AuthenticatedPrincipal

import spock.lang.Specification
import yakworks.security.spring.saml.SpringSamlUser
import yakworks.security.spring.user.SpringUser
import yakworks.security.user.BasicUserInfo

class SpringSamlUserSpec extends Specification {

    void "create SamlUser"() {
        when:
        def samlPrincipal = new DefaultSaml2AuthenticatedPrincipal("adminFromSaml@yak.com", [a:['b']])
        samlPrincipal.relyingPartyRegistrationId = 'okta'
        def su = SpringUser.of(
            BasicUserInfo.create(id: 1, name: "joe", username: "admin@yak.com", email: "admin2@y.com", roles: ['ROLE1', 'ROLE2'], displayName: "foo")
        )
        assert su.id == 1
        assert su.username == "admin@yak.com"
        def samlUser = SpringSamlUser.of(samlPrincipal, su)

        then:
        samlUser.username == "admin@yak.com"
        // samlUser.userProfile.userName == "adminFromSaml@yak.com"
        // samlUser.userProfile.a == 'b'
        ['id', 'username', 'name', 'displayName', 'email', 'roles'].each{
            assert su[it] == samlUser[it]
        }
        samlUser.relyingPartyRegistrationId == samlPrincipal.relyingPartyRegistrationId
        //samlUser.attributes == samlPrincipal.attributes
    }

}
