/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.security.audit.AuditStampTrait

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
class AppUserToken  implements AuditStampTrait, RepoEntity<AppUserToken>, Serializable {

    String tokenValue
    String username
    LocalDateTime expiresAt

    static mapping =  orm {
        version false
    }

    /** IssuedAt is createdDate.toInstant*/
    Instant getIssuedAt(){
        //Convert to instance, zero offset / UTC+0
        return createdDate.toInstant(ZoneOffset.UTC)
    }
}
