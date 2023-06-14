/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.security.audit.AuditStamp

@Entity
@AuditStamp
@GrailsCompileStatic
class ContactEmail implements RepoEntity<ContactEmail>, Serializable {

    String kind //FIXME is this used?
    String address
    Boolean isPrimary = false

    static Map includes = [
        stamp: ['id', 'address', 'kind']  //picklist or minimal for joins
    ]

    static belongsTo = [contact: Contact]

    static mapping = {
        cache "nonstrict-read-write"
        contact column: "contactId"
    }

    static constraintsMap = [
        kind:[d: 'future use', maxSize: 50],
        address:[d: 'The email addy', nullable: false, maxSize: 255],
        isPrimary:[d: 'If this is the contacts primary email', nullable: false, required: false],
    ]
}
