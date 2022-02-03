/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.hibernate.criteria.CreateCriteriaSupport
import gorm.tools.model.NameNum
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.rally.tag.model.HasTags
import yakworks.rally.tag.model.Tag

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Org implements NameNum, GormRepoEntity<Org, OrgRepo>, HasTags, CreateCriteriaSupport, Serializable {

    String  comments
    Long    companyId
    Long    orgTypeId
    OrgType type
    Boolean inactive = false  // no logic attached. Just a flag for reports for now.

    // -- Associations --
    //primary/key contact, invoices will get emailed/mailed here and this is the primary for collections followup, etc..
    Contact contact
    OrgFlex flex
    OrgInfo info
    Location location
    OrgSource source //originator source record

    OrgCalc calc
    OrgMember member
    //this makes finders like OrgMember.findByBranch(branch) work, without it gets confused and fails
    static mappedBy = [member: "org"]

    // OrgDynamicCalc formulaz

    static constraintsMap = [
        num: [d: 'Unique alpha-numeric identifier for this organization', example: 'SPX-321'],
        name: [d: 'The full name for this organization', example: 'SpaceX Corp.'],
        type:[ d: 'The type of org', example: 'Customer',
             nullable: false, bindable: false],
        comments:[ d: 'A user visible comment', example: 'Lorem ipsum'],
        companyId:[ d: 'Company id this org belongs to', example: 2],
        inactive:[ d: 'indicator for an Org that is no longer active'],
        //associations
        flex:[ d: 'User flex fields', nullable: true],
        info:[ d: 'Info such as phone and website for an organization'],
        contact:[ d: 'The default or key Contact for this organization',
             bindable: false, oapi:[read: true, create: ['$ref'], update: ['id']]
        ],
        source:[ description: 'Originator source info, used when this is sourced externally',
             bindable: false, oapi:[read: true, create: ['source', 'sourceType', 'sourceId']]
        ],
        location:[ description: 'The primary organization address info',
             bindable: false, oapi:[read: true, edit: ['$ref']]
        ],
        calc:[ description: 'Calculated fields',
             bindable: false, editable: false],
        member:[ description: 'Dimension hierarchy fields',
             bindable: false, oapi:[read: true, edit: ['$ref']]
        ],
        locations: [d: "List of locations", validate: false ]  //XXX add tests
    ]

    static mapping = {
        id generator: 'assigned'
        orgTypeId column: 'orgTypeId', insertable: false, updateable: false
        type column: 'orgTypeId', enumType: 'identity'
        flex column: 'flexId'
        info column: 'infoId'
        contact column: 'contactId'
        location column: 'locationId'
        source column: 'orgSourceId'
        calc column: 'calcId'
        member column: 'memberId'
        // formulaz insertable: false, updateable: false , column:'id', lazy: true
    }

    //gorm event
    def beforeInsert() {
        //just in case validation and repo are bypassed during creation make sure there is an id
        getRepo().generateId(this)
    }

    boolean isOrgType(OrgType ote){
        this.type == ote
    }

    List<Location> getLocations(){
        Location.listByOrgOnly(this)
    }

    @Override //hasTags trait
    List<Tag> getTags() {
        OrgTag.listTags(this)
    }

    /**
     * quick shortcut to make an Org. unlike create, this returns and UNSAVED new entity
     */
    static Org of(String num, String name, Long orgTypeId, Long companyId = Company.DEFAULT_COMPANY_ID) {
        of(num, name, OrgType.get(orgTypeId), companyId)
    }

    /**
     * quick shortcut to make an Org. unlike create, this returns and UNSAVED new entity
     */
    static Org of(String num, String name, OrgType orgType, Long companyId = Company.DEFAULT_COMPANY_ID) {
        def o = new Org(num: num, name: name, companyId: companyId)
        o.type = orgType
        return o
    }
}
