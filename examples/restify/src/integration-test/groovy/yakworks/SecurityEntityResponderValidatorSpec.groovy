package yakworks

import gorm.tools.hibernate.QueryConfig
import gorm.tools.mango.api.QueryArgs
import spock.lang.Specification
import yakworks.rest.gorm.responder.EntityResponderValidator
import yakworks.rest.gorm.responder.SecurityEntityResponderValidator
import yakworks.security.gorm.UserSecurityConfig
import yakworks.testing.gorm.CurrentTestUser

class SecurityEntityResponderValidatorSpec extends Specification {

    void "sanity check"() {
        setup:
        EntityResponderValidator validator = new SecurityEntityResponderValidator()
        validator.userSecurityConfig = userSecurityConfig
        validator.queryConfig = new QueryConfig(max:100, timeout:100)
        validator.currentUser = new CurrentTestUser()

        QueryArgs args = QueryArgs.of(max:1000)

        when:
        validator.validate(args)

        then: "this user can do max 500"
        args.timeout == 500
        args.pager.max == 500

        when: "user supplied max is smaller thn configured"
        args = QueryArgs.of(max:50)
        validator.validate(args)

        then:
        args.pager.max == 50 //should not foce max to 500 from config

        when: "user does not have extended query config"
        validator.currentUser.user.username = "someother"
        args = QueryArgs.of(max:500)
        validator.validate(args)

        then: "default query max is applied to all others"
        args.pager.max == 100
        args.timeout == 100
    }


    UserSecurityConfig getUserSecurityConfig() {
        def config = new UserSecurityConfig()
        config.users = ["testuser": new UserSecurityConfig.UserConfig(query:new QueryConfig(max:500, timeout:500))]

        return config
    }
}
