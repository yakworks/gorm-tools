package yakity.security

import org.springframework.beans.factory.annotation.Autowired

import yakworks.security.testing.SecuritySeedData

class BootStrap {

    @Autowired SecuritySeedData securitySeedData

    def init = { servletContext ->
        securitySeedData.fullMonty()
    }

    def destroy = {
    }
}
