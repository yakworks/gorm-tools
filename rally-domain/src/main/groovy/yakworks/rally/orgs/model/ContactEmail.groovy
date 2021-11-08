/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@AuditStamp
@GrailsCompileStatic
class ContactEmail implements RepoEntity<ContactEmail>, Serializable {

    String kind //XXX is this used?
    String address
    Boolean isPrimary = false

    static belongsTo = [contact: Contact]

    static mapping = {
        cache true
        contact column: "contactId"
    }

    static constraintsMap = [
        kind:[ description: 'future use', maxSize: 50],
        address:[ description: 'The email addy', nullable: false, maxSize: 50],
        isPrimary:[ description: 'If this is the contacts primary email', nullable: false, required: false],
    ]
}
