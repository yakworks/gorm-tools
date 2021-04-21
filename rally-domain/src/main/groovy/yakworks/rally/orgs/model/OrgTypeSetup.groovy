/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import javax.persistence.Transient

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.common.NameDescription

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class OrgTypeSetup implements NameDescription, RepoEntity<OrgTypeSetup>, Serializable {

    Boolean inactive = false

    static mapping = {
        table 'OrgType'
        cache true
        id generator: 'assigned'
    }

    static constraints = {
        NameDescriptionConstraints(delegate)
    }

    @Transient
    OrgType getOrgType(){
        OrgType.get(id)
    }
}
