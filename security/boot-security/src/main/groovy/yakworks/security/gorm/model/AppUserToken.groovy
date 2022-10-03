/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.security.audit.AuditStampTrait

@Entity
@GrailsCompileStatic
class AppUserToken  implements AuditStampTrait, RepoEntity<AppUserToken>, Serializable {

    String tokenValue
    String username

    static mapping = {
        version false
    }
}
