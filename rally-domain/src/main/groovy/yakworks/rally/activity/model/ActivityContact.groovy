/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import gorm.tools.repository.model.CompositeRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.orgs.model.Contact

@Entity
@GrailsCompileStatic
class ActivityContact implements CompositeRepoEntity<ActivityContact>, Serializable {
    static belongsTo = [activity: Activity, contact: Contact]

    static mapping = {
        version false
        id composite: ['activity', 'contact']
        table 'ActivityContact'
        activity column: 'activityId'
        contact column: 'personId'
    }

    static constraints = {
        activity nullable: false
        contact nullable: false
    }

    static boolean existsByContact(Contact contact){
        countByContact(contact)
    }
}
