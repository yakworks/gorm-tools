/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import java.time.LocalDateTime

import groovy.transform.CompileStatic

import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.Errors

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.mango.api.QueryArgs
import gorm.tools.model.Persistable
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.validation.Rejector
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink
import yakworks.security.user.CurrentUser

import static yakworks.rally.activity.model.Activity.Kind as ActKind

@GormRepository
@CompileStatic
class ActivityRepo extends LongIdGormRepo<Activity> {

    @Autowired ActivityLinkRepo activityLinkRepo
    @Autowired AttachmentRepo attachmentRepo
    @Autowired CurrentUser currentUser
    @Autowired ProblemHandler problemHandler

    @RepoListener
    void beforeValidate(Activity activity, Errors errors) {
        updateNameSummary(activity)
        validateActDate(activity, errors)
    }

    void validateActDate(Activity activity, Errors errors) {
        if(activity.isNew()) {
            if(!activity.actDate) {
                activity.actDate = LocalDateTime.now()
            }
        } else if(activity.hasChanged('actDate')) {
            //actDate can not be updated.
            Rejector.of(activity, errors).withError('actDate', activity.actDate, 'error.notupdateable', [name:"actDate"])
        }
    }

    @RepoListener
    void beforeBind(Activity activity, Map data, BeforeBindEvent e) {
        fixUpTaskParams(data)
        if (e.isBindUpdate()) {
            if (activity.note && data.name) {
                activity.note.body = data.name
                activity.note.persist()
            }
        }
    }

    @RepoListener
    void afterBind(Activity activity, Map data, AfterBindEvent e) {
        assignOrg(activity, data)
    }

    @RepoListener
    void beforeRemove(Activity activity, BeforeRemoveEvent e) {
        if (activity.note) {
            ActivityNote note = activity.note
            activity.note = null
            activity.persist(flush: true)
            note.delete()
        }

        TagLink.remove(activity)

        AttachmentLink.repo.remove(activity)
        //FIXME missing removal for attachments if its not linked to anything else
        //  meaning attachment should also be deleted if it only exists for this activity
        ActivityLink.repo.remove(activity)
        ActivityContact.repo.remove(activity)

    }

    @RepoListener
    void beforePersist(Activity activity, BeforePersistEvent e) {
        if(e.data) {
            Map data = e.data
            addRelatedDomainsToActivity(activity, data)
        }
        if (activity.task) {
            //setup defaults for status and kind
            if (!activity.task.status) activity.task.status = TaskStatus.OPEN
            if (!activity.task.taskType) activity.task.taskType = TaskType.TODO
        }
    }

    /**
     * Called after persist if its had a bind action (create or update) and it has data
     * creates or updates One-to-Many associations for this entity.
     */
    @Override
    void doAfterPersistWithData(Activity activity, PersistArgs args) {
        Map data = args.data
        if(data.attachments) doAttachments(activity, data.attachments)
        if(data.contacts != null) ActivityContact.addOrRemove(activity, data.contacts)
        if(data.tags != null) TagLink.addOrRemoveTags(activity, data.tags)

        if(args.bindAction?.isCreate()){
            if(data.linkedId && data.linkedEntity) {
                activityLinkRepo.create(data.linkedId as Long, data.linkedEntity as String, activity)
            } else if(data.links) {
                assert data.links instanceof List<Map>
                doLinks(activity, data.links as List<Map>)
            }
        }
    }

    /**
     * Override query for custom search for Tags and ActivityLinks
     */

    MangoDetachedCriteria<Activity> queryOld(QueryArgs queryArgs, @DelegatesTo(MangoDetachedCriteria)Closure closure) {
        Map crit = queryArgs.criteriaMap
        DetachedCriteria actLinkExists

        //NOTE: tags are handled in the TagsMangoCriteriaEventListener

        if(crit.linkedId && crit.linkedEntity) {
            Long linkedId = crit.remove('linkedId') as Long //remove so they dont flow through to query
            String linkedEntity = crit.remove('linkedEntity') as String
            actLinkExists = getActivityLinkCriteria(linkedId, linkedEntity)
        }
        MangoDetachedCriteria<Activity> detCrit = getQueryService().query(queryArgs, closure)

        if(actLinkExists != null) {
            detCrit.exists(actLinkExists.id())
        }

        // detCrit.order('createdDate', 'desc')

        return detCrit
    }

    void assignOrg(Activity activity, Map data) {
        // data.orgId wins if its set, only do lookup if its not set
        if (!data.orgId) {
            if (data.org && data.org instanceof Map) {
                activity.org = Org.repo.findWithData(data.org as Map)
            }
            else if(data.org && data.org instanceof Org){
                activity.org = (Org)data.org
            }
        }
    }

    void updateNameSummary(Activity activity) {
        //title to 255
        if (activity.name?.length() > 255) {
            activity.name = StringUtils.abbreviate(activity.name, 255)
        }

        //update name
        if (activity.kind == ActKind.Note && activity.note) {
            int endChar = activity.note.body.trim().length()
            activity.name = (endChar > 255) ? activity.note.body.trim().substring(0, 251) + " ..." : activity.note.body.trim()
        }

    }

    // This adds the realted and children entities from the params to the Activity
    // called in afterBind
    void addRelatedDomainsToActivity(Activity activity, Map data) {

        Map task = data.task as Map

        if (!data.kind && task?.dueDate) {
            activity.kind = ActKind.Todo
        }

        String name = (data.name as String)?.trim()
        if (!activity.note && name?.length() > 255 ) {
            addNote(activity, name)
        }

        if (activity.note && !activity.note.body && data.name) {
            activity.note.body = data.name
            //activity.note.persist() Dont save it here, it will be cascaded
        }

    }

    // This adds the realted and children entities from the params to the Activity
    // called in afterBind
    void doAttachments(Activity activity, Object attData) {
        List attachments = attachmentRepo.createOrUpdate(attData as List)
        //FIXME this is not right
        attachments.each { Attachment attachment ->
            AttachmentLink.create(activity, attachment)
        }
        activity.setHasAttachments(true)
    }

    void doLinks(Activity activity, List<Map> links) {
        links.each { link ->
            activityLinkRepo.create(link['linkedId'] as Long, link['linkedEntity'] as String, activity)
        }
    }

    ActivityNote addNote(Activity act, String body, String contentType = "plain") {
        if (!act.note) {
            act.note = new ActivityNote()
        }
        act.note.body = body
        act.note.contentType = contentType
        return act.note
    }

    void fixUpTaskParams(Map params) {
        //if there is no due date then assume its not a task and remove all the task stuff if it exists
        Map taskParams = params.task as Map
        if (!taskParams || taskParams.dueDate == null) {
            //params.remove "title"
            params.remove "task"
        }
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
            // setAlias 'actLink'
            //"activity.id" is the field on ActivityLink and "activity_.id" is the alias on activity
            eqProperty("activity.id", "activity_.id")
            eq("linkedId", linkedId)
            eq("linkedEntity", linkedEntity)
        }
    }

}
