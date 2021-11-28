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

    String kind //XXX what is this, should be enum?
    String num
    Boolean isPrimary = false

    static Map includes = [
        stamp: ['id', 'num', 'kind']  //picklist or minimal for joins
    ]
    static belongsTo = [contact: Contact]
    static mapping = {
        cache true
        contact column: 'contactId'
    }

    static constraintsMap = [
        kind:[ description: 'future use', blank: false, maxSize: 50],
        num:[ description: 'future use', blank: false, maxSize: 50],
        isPrimary:[ description: 'if this is the contacts primary number', nullable: false, required: false],
    ]
}
