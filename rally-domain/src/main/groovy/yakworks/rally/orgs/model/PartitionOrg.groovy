/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.model.NameNum
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.orgs.repo.PartitionOrgRepo
import yakworks.security.audit.AuditStamp

/**
 * Represents an Org which has orgType=partitionOrgType
 */
@Entity
@AuditStamp
@GrailsCompileStatic
class PartitionOrg implements NameNum, RepoEntity<PartitionOrg>, Serializable {

    Org org

    static mapping = {
        //keep generator assigned, instead of foreign, so that id can be assigned, and partition org can be saved
        //during orgrepo.beforePersist even before org is persisted yet, or else, it needs a persisted org
        id generator: 'assigned'
        org insertable: false, updateable: false, column: 'id'
    }

    static constraintsMap = [
        num: [d: 'Unique alpha-numeric identifier for this organization', example: 'SPX-321', nullable: false, maxSize: 50],
        name: [d: 'The full name for this organization', example: 'SpaceX Corp.', nullable: false, maxSize: 255]
    ]

    static PartitionOrgRepo getRepo() { return (PartitionOrgRepo) RepoLookup.findRepo(this) }
}
