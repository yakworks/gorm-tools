/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.audit.AuditStamp

@Entity
@AuditStamp
@IdEqualsHashCode
// @ManagedEntity
@GrailsCompileStatic
class OrgInfo implements RepoEntity<OrgInfo>, Serializable {
    static belongsTo = [Org]

    String phone   //primary hq switchboard
    String fax     //primary fax
    String website // website home page

    static mapping = {
        id generator: 'assigned'
    }

    static constraintsMap = [
        phone:[d: 'primary hq number, switchboard', maxSize: 255],
        fax:[d: 'primary fax', maxSize: 255],
        website:[d: 'website home page', maxSize: 255]
    ]

}
