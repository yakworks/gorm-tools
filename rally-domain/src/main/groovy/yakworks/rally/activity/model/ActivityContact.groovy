/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.activity.repo.ActivityContactRepo
import yakworks.rally.orgs.model.Contact

@Entity
@GrailsCompileStatic
class ActivityContact implements GormRepoEntity<ActivityContact, ActivityContactRepo>, Serializable {
    Activity activity
    Contact contact

    static mapping = {
        version false
        id composite: ['activity', 'contact']
        table 'ActivityContact'
        activity column: 'activityId'
        contact column: 'personId'
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof ActivityContact) {
            return other.getContactId() == getContactId() && other.getActivityId() == getActivityId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getContactId()) { hashCode = HashCodeHelper.updateHash(hashCode, getContactId()) }
        if (getActivityId()) { hashCode = HashCodeHelper.updateHash(hashCode, getActivityId()) }
        hashCode
    }
}
