package yakity.security

import yakworks.security.gorm.model.AppUser
import yakworks.security.gorm.testing.SecuritySeedData

class BootStrap {

    def init = { servletContext ->
        //add one that maps to our Okta dev
        AppUser.withTransaction {
            SecuritySeedData.fullMonty()
            AppUser.repo.flush()
            AppUser admin = new AppUser([
                id: (Long)5, username: "developers@9ci.com", email: "developers@9ci.com", orgId: 2
            ]).persist()

            admin.addRole('ADMIN', true)
            admin.addRole('MANAGER', true)
        }
    }

    def destroy = {
    }
}
