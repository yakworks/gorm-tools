/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.model.NameNum
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@AuditStamp
@GrailsCompileStatic
class Company implements NameNum, RepoEntity<Company>, Serializable {
    public static final Long DEFAULT_COMPANY_ID = 2
    public static final Long BAD_DEBT_COMPANY_ID = 5

    String website //FIXME whats this for? can't we get it from org.info
    String sourceId //FIXME whats this for?
    static belongsTo = [org: Org]

    static mapping = {
        cache true
        id generator: 'foreign', params: [property: 'org']
        org insertable: false, updateable: false, column: 'id'
    }

    static constraintsMap = [
        website:[ description: 'The company website', nullable: true],
        sourceId:[ description: 'The source identifier for this company', nullable: true]
    ]

}
