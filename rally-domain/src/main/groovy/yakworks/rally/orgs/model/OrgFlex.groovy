/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.common.FlexTrait

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class OrgFlex implements FlexTrait, RepoEntity<OrgFlex>, Serializable {
    static belongsTo = [Org]

    static mapping = {
        id generator: 'assigned'
    }

}
