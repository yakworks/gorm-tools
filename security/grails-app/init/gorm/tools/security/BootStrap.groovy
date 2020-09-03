/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security

import gorm.tools.security.domain.SecRole
import gorm.tools.security.domain.SecRoleUser
import gorm.tools.security.domain.SecUser
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class BootStrap {

    def init = { servletContext ->
        SecUser.withTransaction({
            SecUser user = new SecUser(name: "admin", login: "admin", email: "admin@9ci.com")
            user.pass = "admin"
            user.encodePassword()
            user.persist()
            assert user.id == 1

            SecRole admin = SecRole.create([name: SecRole.ADMINISTRATOR])
            SecRole power = SecRole.create([name: "Power User"])
            SecRole guest = SecRole.create([name: "Guest"])

            SecRoleUser.create(user, admin, true)

            assert admin.id == 1
            assert power.id == 2
        })
    }

}
