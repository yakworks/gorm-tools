/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import yakworks.rally.seed.RallySeed
import yakworks.security.gorm.model.AppUser

class BootStrap {

    def init = { servletContext ->
        RallySeed.fullMonty()
        addOktaUser()
    }

    //add the developers@9ci.com so saml works
    void addOktaUser(){
        //add one that maps to our Okta dev
        AppUser.withTransaction {
            AppUser.repo.flush()
            AppUser admin = new AppUser([
                id: (Long)6, username: "developers@9ci.com", email: "developers@9ci.com", orgId: 2
            ]).persist()
            admin.addRole('ADMIN', true)
            admin.addRole('MANAGER', true)
            //used for github user testing
            // AppUser admin2 = new AppUser([
            //     id: (Long)9, username: "josh2@9ci.com", email: "josh2@9ci.com", orgId: 2
            // ]).persist()
            // admin2.addRole('ADMIN', true)
            // admin2.addRole('MANAGER', true)
        }
    }

    def destroy = {
    }

}
