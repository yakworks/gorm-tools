/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import yakworks.rally.common.FlexTrait

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class ContactFlex implements FlexTrait, RepoEntity<ContactFlex>, Serializable{

    static belongsTo = [contact: Contact]

    static mapping = {
        id generator: 'foreign', params: [property: 'contact']
    }

}
