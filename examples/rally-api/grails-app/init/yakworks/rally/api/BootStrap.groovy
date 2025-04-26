/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import yakworks.rally.orgs.model.Contact
import yakworks.rally.seed.RallySeed
import yakworks.security.gorm.model.AppUser

class BootStrap {

    def init = { servletContext ->
        def contacts
        AppUser.withTransaction {
            contacts = Contact.list()
        }
        println "************************ contacts size : $contacts.size()"
        if(!contacts) {
            RallySeed.fullMonty()
            addOktaUser()
        }
        AppUser.withTransaction {
            contacts = Contact.list()
        }
        println "************************ after contacts size : $contacts.size()"
    }

    //add the developers@9ci.com so saml works
    void addOktaUser(){
        //add one that maps to our Okta dev
        AppUser.withTransaction {
            AppUser.repo.flush()
            AppUser admin = new AppUser([
                id: 6L, username: "developers@9ci.com", email: "developers@9ci.com", orgId: 2
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
