/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.GormRepoEntity
import gorm.tools.source.SourceTrait
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.repo.OrgSourceRepo

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class OrgSource implements GormRepoEntity<OrgSource, OrgSourceRepo>, SourceTrait, Serializable {
    //Org org //belongsTo org but since it is both a 1toMany and and association on the org we dont use the belongsTo
    Long orgId

    //edit sequence number from the source system.
    String sourceVersion
    //flag that Org was generated with this source (it should be true for source = org.orgSource)
    Boolean originator = false
    // denormalized orgType so we can have unique index within org type (source, sourceId and orgType)
    OrgType orgType

    static constraintsMap = [
        orgId:[ description: 'The id of the org this is for', example: 954,
            nullable: false],
        orgType:[ description: 'denormalized orgType so we can have unique index within org type (sourceType, sourceId and orgType)',
            nullable: false, example: 'Customer', editable: false, required: false],
        sourceVersion:[ description: 'the version of the last edit in source system',
            nullable: true, example: '912'],
        originator:[ description: 'indicates this source was the creator of this org, should only be 1 per Org',
            nullable: false, required: false]
    ]

    //unique index within org type (source, sourceId and orgType)
    static mapping = {
        id generator: 'assigned'
        orgId  column: 'orgId' //, insertable: false, updateable: false
        orgType column: 'orgTypeId', enumType: 'identity'
    }


    //just in case validation and repo are bypassed during creation make sure its gets an id
    def beforeInsert() {
        repo.generateId(this)
    }

}
