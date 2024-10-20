package yakworks.security.gorm.api

import gorm.tools.mango.api.QueryArgs
import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException
import yakworks.gorm.config.QueryConfig
import yakworks.testing.gorm.CurrentTestUser

class UserQueryArgsValidatorSpec extends Specification {

    void "sanity check"() {
        setup:
        UserQueryArgsValidator validator = new UserQueryArgsValidator()
        validator.userSecurityConfig = userSecurityConfig
        validator.queryConfig = new QueryConfig(max:100, timeout:100)
        validator.currentUser = new CurrentTestUser()

        QueryArgs args = QueryArgs.of(max:1000)

        when:
        validator.validate(args)

        then:
        DataProblemException ex = thrown()
        ex.code == "error.query.max"

        when: "user supplied max is smaller thn configured"
        args = QueryArgs.of(max:50)
        validator.validate(args)

        then:
        args.pager.max == 50 //should not foce max to 500 from config

        when: "user does not have extended query config"
        validator.currentUser.user.username = "someother"
        args = QueryArgs.of(max:500)
        validator.validate(args)

        then:
        ex = thrown()
        ex.code == "error.query.max"

    }


    UserSecurityConfig getUserSecurityConfig() {
        def config = new UserSecurityConfig()
        config.users = ["testuser": new UserSecurityConfig.UserConfig(query:new QueryConfig(max:500, timeout:500))]

        return config
    }
}
