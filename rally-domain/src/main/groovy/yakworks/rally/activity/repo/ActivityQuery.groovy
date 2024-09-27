/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import javax.persistence.criteria.JoinType

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.tag.TaggableQueryService
import yakworks.security.user.CurrentUser

import static yakworks.rally.activity.model.Activity.VisibleTo

@Service @Lazy
@CompileStatic
@Slf4j
class ActivityQuery extends TaggableQueryService<Activity> {

    @Autowired ActivityLinkRepo activityLinkRepo
    @Autowired CurrentUser currentUser

    ActivityQuery() {
        super(Activity)
    }

    /**
     * override to add tags.
     * This runs after the tidyMap has been run which puts it into a standard format
     */
    @Override
    void applyCriteria(MangoDetachedCriteria<Activity> mangoCriteria){
        Map crit = mangoCriteria.criteriaMap
        DetachedCriteria actLinkExists
        if(crit.linkedId && crit.linkedEntity) {
            //should be in format [linkedId:['$eq':123]] since its gone through the tidy map.

            Long linkedId = (crit.remove('linkedId') as Map).remove('$eq') as Long //remove so they dont flow through to query
            String linkedEntity = (crit.remove('linkedEntity') as Map).remove('$eq') as String
            actLinkExists = getActivityLinkCriteria(linkedId, linkedEntity)
            if(actLinkExists != null) {
                mangoCriteria.exists(actLinkExists.id())
            }
        }
        super.applyCriteria(mangoCriteria)
    }

    @ReadOnly
    boolean hasActivityWithAttachments(Persistable<Long> linkedEntity, Activity.Kind kind = null) {
        def actLinkExists = getActivityLinkCriteria(linkedEntity.getId(), linkedEntity.class.simpleName)

        def laQuery = Activity.query {
            setAlias 'activity_'
            if(kind) {
                eq("kind", kind)
            }
            exists actLinkExists.id()
        }

        def attachExists = AttachmentLink.query {
            setAlias 'attachLink'
            eqProperty("linkedId", "activity_.id")
            eq("linkedEntity", 'Activity')
        }

        return laQuery.exists(attachExists.id()).count()
    }

    /**
     * gets the criteria for ActivityLinks that can be used in an exists
     */
    DetachedCriteria<ActivityLink> getActivityLinkCriteria(Long linkedId,  String linkedEntity) {
        return ActivityLink.query {
            //"activity.id" is the field on ActivityLink and "activity_.id" is the alias on activity
            eqProperty("activity.id", "activity_.id")
            eq("linkedId", linkedId)
            eq("linkedEntity", linkedEntity)
        }
    }

    @Deprecated //unused, here for reference on custArea
    DetachedCriteria<Activity> zzzgetActivityByLinkedCriteria(Long linkedId,  String linkedEntity, boolean custArea = false) {
        def actLinkExists = getActivityLinkCriteria(linkedId, linkedEntity)

        return Activity.query {
            setAlias 'activity_' //match default
            createAlias('task', 'task')
            join('task', JoinType.LEFT)
            exists actLinkExists.id()
            or {
                isNull("task")
                le("task.state", 1)
            }
            or {
                eq("visibleTo", VisibleTo.Everyone)
                if (!custArea) {
                    ne("visibleTo", VisibleTo.Owner)
                    and {
                        eq("visibleTo", VisibleTo.Owner)
                        eq("createdBy", currentUser.userId)
                    }
                }
            }

        }
    }

}
