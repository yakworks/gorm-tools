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

@Entity
@AuditStamp
@IdEqualsHashCode
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
        phone:[ description: 'primary hq number, switchboard'],
        fax:[ description: 'primary fax'],
        website:[ description: 'website home page']
    ]

}
