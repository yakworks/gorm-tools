/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.LinkedEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.activity.repo.ActivityLinkRepo

@Entity
@GrailsCompileStatic
class ActivityLink implements LinkedEntity, RepoEntity<ActivityLink>, Serializable {
    static belongsTo = [activity: Activity]

    static mapping = {
        id composite: ['activity', 'linkedId', 'linkedEntity']
        version false
        activity column: 'activityId', cache: true, fetch: 'join'
    }

    static ActivityLinkRepo getRepo() { return (ActivityLinkRepo) RepoLookup.findRepo(this) }

    static ActivityLink create(Persistable linkedEntity, Activity act) {
        getRepo().create(linkedEntity, act)
    }

    static ActivityLink get(Persistable entity, Activity act) {
        getRepo().get(entity, act)
    }

    static List<ActivityLink> list(Persistable entity) {
        getRepo().list(entity)
    }

    static List<Activity> listActivities(Persistable entity) {
        getRepo().listRelated(entity)
    }

    static boolean exists(Persistable entity, Activity act) {
        getRepo().exists(entity, act)
    }

    static MangoDetachedCriteria<ActivityLink> queryFor(Persistable entity) {
        getRepo().queryFor(entity)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof ActivityLink) {
            return other.getLinkedId() == getLinkedId() && other.getLinkedEntity() == getLinkedEntity() && other.getActivityId() == getActivityId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getLinkedId()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedId()) }
        if (getLinkedEntity()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedEntity()) }
        if (getActivityId()) { hashCode = HashCodeHelper.updateHash(hashCode, getActivityId()) }
        hashCode
    }
}
