/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.Persistable
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

    static ActivityContact create( Activity act, Contact con, Map args = [:]) {
        getRepo().create(act, con, args)
    }

    static List<ActivityContact> list(Persistable entity) {
        getRepo().list(entity)
    }

    static List<Contact> listContacts( Activity act ) {
        getRepo().listRelated(act)
    }

    static List<ActivityContact> addOrRemove(Activity act, Object itemParams){
        getRepo().addOrRemove(act, itemParams)
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
