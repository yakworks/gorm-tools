/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class SecLoginHistory implements Serializable {

    SecUser user
    Date loginDate
    Date logoutDate

    static mapping = {
        table 'SecLoginHistory'
        user column: 'userId'
        version false
    }

    static constraints = {
        loginDate nullable: true
        logoutDate nullable: true
    }
}
