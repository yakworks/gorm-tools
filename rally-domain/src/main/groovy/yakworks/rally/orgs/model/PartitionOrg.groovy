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
 * See docs in OrgProps for partion.
 * This holds copy of the partition Org since it joined so often.
 * PartitionOrg is Division at CED and RNDC for example. There might be 20-50 of them but there are millions of Orgs since
 * there are that many customers. PartitionOrg is frequently if not always joined to ArTran and Customer for filtering and validation.
 * This way we are joining and filtering on a table with 20 rows instead 2 million.
 */
@Entity
@AuditStamp
@GrailsCompileStatic
class PartitionOrg implements NameNum, RepoEntity<PartitionOrg>, Serializable {

    /** read only link to the Org using this id */
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

    static PartitionOrg getByOrg(Org org) {
        getRepo().getByOrg(org)
    }
}
