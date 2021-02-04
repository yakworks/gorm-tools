/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.common.NameNum
import yakworks.rally.common.SourceType
import yakworks.rally.orgs.repo.OrgRepo
import yakworks.rally.tag.model.Taggable

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Org implements NameNum, GormRepoEntity<Org, OrgRepo>, Taggable<OrgTag>, Serializable {

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

    static mapping = {
        id generator: 'assigned'
        orgTypeId column: 'orgTypeId', insertable: false, updateable: false
        type column: 'orgTypeId', enumType: 'identity'
        flex column: 'flexId'
        info column: 'infoId'
        contact column: 'contactId'
        location column: 'locationId'
        source column: 'orgSourceId'
    }

    static constraints = {
        NameNumConstraints(delegate)
        type nullable: false, bindable: false
        comments nullable: true
        flex nullable: true
        info nullable: true
        inactive nullable: false
        contact nullable: true, bindable: false
        source nullable: true, bindable: false
        companyId nullable: false
        location nullable: true, bindable: false
    }

    //gorm event
    def beforeInsert() {
        //just in case validation and repo are bypassed during creation make sure there is an id
        getRepo().generateId(this)
    }

    /**
     * quick shortcut to create an Org. return the unsaved new entity
     */
    static Org create(String num, String name, OrgType orgType) {
        def o = new Org(num: num, name: name)
        o.type = orgType
        return o
    }

    /**
     * shortcut to create the OrgSource for this. See the OrgSourceRepo.createSource
     */
    OrgSource createSource(SourceType sourceType = SourceType.App) {
        getRepo().createSource(this, sourceType)
    }

    boolean isOrgType(OrgType ote){
        this.type == ote
    }

    List<Location> getLocations(){
        Location.listByOrgOnly(this)
    }

}
