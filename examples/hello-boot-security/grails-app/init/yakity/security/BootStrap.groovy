package yakity.security

import org.springframework.beans.factory.annotation.Autowired

import yakworks.security.gorm.model.AppUser
import yakworks.security.testing.SecuritySeedData

class BootStrap {

    @Autowired SecuritySeedData securitySeedData

    def init = { servletContext ->
        securitySeedData.fullMonty()
        //add one that maps to our Okta dev
        AppUser.withTransaction {
            AppUser admin = new AppUser([
                id: (Long)5, username: "developers@9ci.com", email: "developers@9ci.com", orgId: 2
            ]).persist()

            admin.addRole('ADMIN', true)
            admin.addRole('POWER_USER', true)
        }
    }

    def destroy = {
    }
}
