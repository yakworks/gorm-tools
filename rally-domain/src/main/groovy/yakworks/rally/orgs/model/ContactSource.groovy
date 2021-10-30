/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.model.SourceTrait
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@AuditStamp
@GrailsCompileStatic
class ContactSource implements SourceTrait, RepoEntity<ContactSource>, Serializable {

    static belongsTo = [contact: Contact]

    //The id from the outside source or of the collection step, promise or some future workflow template record that generated this
    String sourceVersion //edit sequence number from the source system.

    static mapping = {
        contact column: 'contactId'
    }

    static constraintsMap = [
        sourceVersion:[ nullable: true],
        contact:[ nullable: false]
    ]
}
