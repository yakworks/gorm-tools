/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
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
class PartitionOrg implements NameNum, RepoEntity<PartitionOrg>, Serializable {

    static mapping = {
        id generator:'assigned'
    }

    static constraintsMap = [
        num: [d: 'Unique alpha-numeric identifier for this organization', example: 'SPX-321', nullable: false, maxSize: 50],
        name: [d: 'The full name for this organization', example: 'SpaceX Corp.', nullable: false, maxSize: 255]
    ]
}
