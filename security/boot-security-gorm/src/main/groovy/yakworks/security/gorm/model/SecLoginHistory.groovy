/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.LocalDateTime

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
class SecLoginHistory implements RepoEntity<SecLoginHistory>, Serializable {

    Long userId
    LocalDateTime loginDate
    LocalDateTime logoutDate

    static mapping = orm {
        table 'SecLoginHistory'
        // user column: 'userId'
        version false
    }

    static constraints = {
        loginDate nullable: true
        logoutDate nullable: true
    }
}
