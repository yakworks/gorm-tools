/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class ContactPhone implements RepoEntity<ContactPhone>, Serializable {

    String kind
    String num
    Boolean isPrimary = false

    static belongsTo = [contact: Contact]
    static mapping = {
        cache true
        contact column: 'contactId'
    }

    static constraints = {
        kind blank: false, nullable: true, maxSize: 50
        num blank: false, nullable: true, maxSize: 50
        isPrimary nullable: true
    }
}
