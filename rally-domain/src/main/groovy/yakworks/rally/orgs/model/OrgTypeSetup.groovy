/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import javax.persistence.Transient

import gorm.tools.audit.AuditStamp
import gorm.tools.model.NameCodeDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class OrgTypeSetup implements NameCodeDescription, RepoEntity<OrgTypeSetup>, Serializable {

    Boolean inactive = false

    static mapping = {
        table 'OrgType'
        cache "nonstrict-read-write"
        id generator: 'assigned'
    }

    @Transient
    OrgType getOrgType(){
        OrgType.get(id)
    }
}
