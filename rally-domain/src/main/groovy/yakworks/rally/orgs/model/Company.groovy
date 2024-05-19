/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.model.NameNum
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.security.audit.AuditStamp

@Entity
@AuditStamp
@GrailsCompileStatic
class Company implements NameNum, RepoEntity<Company>, Serializable {
    public static final Long DEFAULT_COMPANY_ID = 2

    static belongsTo = [org: Org]

    static mapping = {
        cache "nonstrict-read-write"
        id generator: 'foreign', params: [property: 'org']
        org insertable: false, updateable: false, column: 'id'
    }

    static constraintsMap = [
        name:[ d: 'Full name for this company', maxSize: 255],
        //website:[d: 'The company website', nullable: true],
        ///sourceId:[d: 'The source identifier for this company', nullable: true, maxSize: 50]
    ]

}
