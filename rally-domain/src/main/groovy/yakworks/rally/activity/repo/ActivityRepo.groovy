/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.repo

import java.time.LocalDateTime
import javax.annotation.Nullable
import javax.inject.Inject
import javax.persistence.criteria.JoinType

import groovy.transform.CompileStatic

import org.apache.commons.lang3.StringUtils

import gorm.tools.api.ProblemHandler
import gorm.tools.api.ApiResults
import gorm.tools.api.result.Result
import gorm.tools.beans.Pager
import gorm.tools.model.Persistable
import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.errors.EntityValidationException
import gorm.tools.repository.events.AfterPersistEvent
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.BeforeRemoveEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.security.services.SecService
import gorm.tools.utils.GormUtils
import grails.gorm.DetachedCriteria
import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import yakworks.commons.lang.Validate
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink

import static yakworks.rally.activity.model.Activity.Kind as ActKind
import static yakworks.rally.activity.model.Activity.VisibleTo

@GormRepository
@CompileStatic
class ActivityRepo implements GormRepo<Activity>, IdGeneratorRepo {

    @Inject @Nullable
    ActivityLinkRepo activityLinkRepo

    @Inject @Nullable
    AttachmentRepo attachmentRepo

    @Inject @Nullable
    SecService secService

    @Inject @Nullable
    ProblemHandler problemHandler

    @RepoListener
    void beforeValidate(Activity activity) {
        if(activity.isNew()) {
            generateId(activity)
        }
        wireAssociations(activity)
        updateNameSummary(activity)
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
    void beforeRemove(Activity activity, BeforeRemoveEvent e) {
        if (activity.note) {
            ActivityNote note = activity.note
            activity.note = null
            activity.persist(flush: true)
            note.delete()
        }

        TagLink.remove(activity)

        AttachmentLink.repo.remove(activity)
        //XXX missing removal for attachments if its not linked to anything else
        //  meaning attachment should also be deleted if it only exists for this activity
        ActivityLink.repo.remove(activity)
        ActivityContact.repo.remove(activity)

    }

    @RepoListener
    void beforePersist(Activity activity, BeforePersistEvent e) {
        generateId(activity)
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

    @RepoListener
    void afterPersist(Activity activity, AfterPersistEvent e) {
        if (e.bindAction && e.data){
            Map data = e.data
            doAssociations(activity, data)
        }
        //FIXME this is a hack so the events for links get fired after data is inserted
        // not very efficient as removes batch inserting for lots of acts so need to rethink this strategy
        // flush()
    }

    void doAssociations(Activity activity, Map data) {
        if(data.attachments) doAttachments(activity, data.attachments)
        if(data.contacts) ActivityContact.addOrRemove(activity, data.contacts)
        if(data.tags) TagLink.addOrRemoveTags(activity, data.tags)

        // now do the links last do events will have the other data
        if (data.arTranId) {
            activityLinkRepo.create(data.arTranId as Long, 'ArTran', activity)
        }
    }


    void wireAssociations(Activity activity) {
        if (activity.note && !activity.note.id) activity.note.id = activity.id
        if (activity.task && !activity.task.id) activity.task.id = activity.id
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

    ActivityNote addNote(Activity act, String body, String contentType = "plain") {
        if (!act.note) {
            act.note = new ActivityNote()
        }
        act.note.body = body
        act.note.contentType = contentType
        return act.note
    }

    void completeTask(Task task, Long completedById) {
        Validate.notNull(completedById, "[completedById]")
        task.bind(status: TaskStatus.COMPLETE,
                state: TaskStatus.COMPLETE.id as Integer,
                completedDate: LocalDateTime.now(),
                completedBy: completedById)

    }

    /**
     * insert a single activity and note for a list of domains.
     * @param targets A list of domains which need to have the activity assigned.
     * @param entityName is the class name. Should be the same as target.getClass().getSimpleName()
     * @param org is an Org to which this target is related.  All targets must be related to the same Org.
     * @param body the note body
     */
    @Transactional
    Activity insertMassNote(List targets, String entityName, Org org, String body) {
        Activity activity = new Activity()
        activity.org = org
        addNote(activity, body)
        updateNameSummary(activity)

        activity.source = entityName
        activity.sourceType = SourceType.App
        activity.persist()

        targets.each { target ->
            activityLinkRepo.create(target['id'] as Long, entityName, activity)
        }

        activity.persist()
        return activity
    }

    /**
     * Insert activities for the given list of target domains
     *
     * @param targets One of the [ArTran, Customer, CustAccount, Payment]
     * @param activityData The data for new activity. Example below.
     *        <pre>
     *        [
     *        name: "The text for note/title/summary"
     *        task: [
     *          dueDate : "2017-04-28",
     *          priority: "10",
     *          state   : "1",
     *          taskType: [id: "1"],
     *          user    : [id: 1, contact: [name: "9ci"]]
     *        ]
     *        attachments:[
     *          name: "test.txt",
     *          tempFileName: tempFileName
     *        ]
     *        ]
     *        </pre>
     * @param source activity source - if the source is from outside
     * @param newAttachments if new attachments should be created for each target
     * @return list of activities
     */
    @Transactional
    List<Activity> insertMassActivity(List targets, Map activityData, String source = null, boolean newAttachments = false) {

        Map<Long, Activity> createdActivities = [:]
        List attachments = []
        List attachmentData = activityData?.attachments as List
        if (attachmentData) {
            attachments = attachmentRepo.createOrUpdate(attachmentData)
            if (targets[0].class.simpleName == "Payment") {
                attachments.each { Attachment att ->
                    String name = activityData?.name
                    att.description = name?.size() > 255 ? name[0..254] : name
                    att.persist()
                }
            }
        }
        List<Activity> activities = []
        targets.eachWithIndex { target, i ->
            String entityName = target.getClass().getSimpleName()
            Org org = (entityName == "ArTran" ? target['customer']['org'] : target['org']) as Org //possible candidates, ArTran,Customer,CustAccount,Payment
            Activity activity
            if (createdActivities[org.id] && entityName != "Payment") {
                activity = createdActivities[org.id]
            } else {
                List copiedAttachments = attachments
                //Here !=0 = for first payment use the original attachments and for all rest of the payments copy it.
                //so same attachments are not shared between payments.
                if (i != 0 && newAttachments) {
                    copiedAttachments = attachments.collect { attachmentRepo.copy(it as Attachment)}
                }
                activity = createActivity(activityData.name.toString(), org, (Map) activityData.task, copiedAttachments, entityName, source)
                createdActivities[org.id as Long] = activity
            }

            Long linkedId = target['id'] as Long
            activityLinkRepo.create(linkedId, entityName, activity)

            activities.add(activity)
        }

        return activities
    }

    /**
     * Creates new activity
     *
     * @param text Text for note body/title/summary (Title and summary will be trimmed to 255 characters)
     * @param org the org for the activity
     * @param task Data for the new task
     * @param attachments list of attachments to attach to this activity
     * @param entityName linked entity name for which the activity is created (Eg. ArTran, Customer etc)
     * @param source activity source -  if this is from outside.
     * @return Activity
     */
    //FIXME this is old and should be deprected
    @Transactional
    Activity createActivity(String text, Org org, Map task, List<Attachment> attachments, String entityName, String source = null) {

        Activity activity = new Activity(
            org         : org,
                name: text,
            source      : entityName,
            sourceType: SourceType.App
        )
        generateId(activity)
        if (task) {
            activity.task = createActivityTask(task)
            activity.kind = activity.task.taskType.kind
        } else {
            addNote(activity, text)
            updateNameSummary(activity)
        }
        attachments?.each { attachment ->
            AttachmentLink.create(activity, attachment)
        }
        activity.persist()
    }

    @Transactional
    Task createActivityTask(Map taskData) {
        TaskType taskType = TaskType.get(taskData.taskType['id'] as Long)
        Task task = new Task()
        task.bind([
            taskType: taskType,
            userId  : (taskData.user ? taskData.user['id'] : null) as Long,
            dueDate : taskData.dueDate,
            priority: taskData.priority,
            state   : taskData.state ? taskData.state : Task.State.Open,
            status  : TaskStatus.getOPEN()]
        )
        return task
    }

    /**
     * quick easy way to create a Todo activity
     */
    @Transactional
    Activity createTodo(Org org, Long userId, String name, String linkedEntity = null,
                        List<Long> linkedIds = null, LocalDateTime dueDate = LocalDateTime.now()) {

        Activity activity = create(org: org, name: name, kind : Activity.Kind.Todo)

        if(linkedIds){
            for(Long linkedId: linkedIds){
                ActivityLink activityLink = new ActivityLink(activity: activity, linkedId: linkedId, linkedEntity: linkedEntity)
                activityLink.persist()
            }
        }

        activity.task = new Task(
            taskType: TaskType.TODO,
            userId  : userId,
            dueDate : dueDate,
            status  : TaskStatus.OPEN
        )
        activity.persist()
        return activity
    }

    void fixUpTaskParams(Map params) {
        //if there is no due date then assume its not a task and remove all the task stuff if it exists
        Map taskParams = params.task as Map
        if (!taskParams || taskParams.dueDate == null) {
            //params.remove "title"
            params.remove "task"
        }
    }

    DetachedCriteria<Activity> linkedActivityCriteria(Persistable linkedEntity, Activity.Kind kind = null) {
        def actLinkExists = ActivityLink.query {
            setAlias 'actLink'
            eqProperty("activity.id", "act.id")
            eq("linkedId", linkedEntity.id)
            eq("linkedEntity", linkedEntity.class.simpleName)
        }

        return Activity.query {
            setAlias 'act'
            if(kind) {
                eq("kind", kind)
            }
            exists actLinkExists.id()
        }
    }

    @ReadOnly
    boolean hasActivityWithAttachments(Persistable entity) {
        def laQuery = linkedActivityCriteria(entity)

        def attachExists = AttachmentLink.query {
            setAlias 'attachLink'
            eqProperty("linkedId", "act.id")
            eq("linkedEntity", 'Activity')
        }

        return laQuery.exists(attachExists.id()).count()
    }

    @ReadOnly
    List<Activity> listByLinked(Long linkedId, String linkedEntity, Map params) {
        Pager pager = new Pager(params)
        def crit = getActivityByLinkedCriteria(linkedId, linkedEntity, params.custArea as boolean, )
        crit.order('createdDate', 'desc')
        List<Activity> activityList = crit.list(max: pager.max, offset: pager.offset)
        return activityList
    }

    DetachedCriteria<Activity> getActivityByLinkedCriteria(Long linkedId,  String linkedEntity, boolean custArea = false) {
        def actLinkExists = ActivityLink.query {
            setAlias 'actLink'
            eqProperty("activity.id", "act.id")
            eq("linkedId", linkedId)
            eq("linkedEntity", linkedEntity)
        }
        return Activity.query {
            setAlias 'act'
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
                        eq("createdBy", secService.userId)
                    }
                }
            }

        }
    }

    @Transactional
    Activity copy(Activity fromAct, Activity toAct) {
        if (fromAct == null) return null

        GormUtils.copyDomain(toAct, fromAct, [createdBy: fromAct['createdBy'], editedBy: fromAct['editedBy']])
        toAct.note = GormUtils.copyDomain(ActivityNote, fromAct.note, [activity: toAct], false)
        toAct.task = GormUtils.copyDomain(Task, fromAct.task, [activity: toAct], false)
        if(fromAct.template) toAct.template = attachmentRepo.copy(fromAct.template)
        if(!toAct.id) toAct.id = generateId()

        //actCopy.persist()

        fromAct.attachments?.each { Attachment attachment ->
            Attachment attachCopy = attachmentRepo.copy(attachment)
            if(attachCopy) {
                AttachmentLink.create(toAct, attachCopy)
            }
        }

        ActivityContact.repo.copyRelated(fromAct, toAct)

        activityLinkRepo.copyLinked(fromAct, toAct)

        toAct.persist()

        TagLink.repo.copyTags(fromAct, toAct)
        return toAct

    }

    /**
     * Copies all activities from given org to target org
     */
    @Transactional
    Result copyToOrg(Org fromOrg, Org toOrg) {
        ApiResults results = ApiResults.OK()
        List<Activity> activities = Activity.findAllWhere(org: fromOrg)

        activities.each { Activity activity ->
            try {
                Activity copy = copy(activity, new Activity(org: toOrg))
                if (copy) {
                    Map queryParams = [edDate: activity['editedDate'], crDate: activity['createdDate'], newid: copy.id]
                    Activity.executeUpdate("update Activity act set act.editedDate=:edDate, act.createdDate=:crDate where act.id=:newid ", queryParams)
                }
            } catch (EntityValidationException e) {
                results << problemHandler.handleException(e)
            }
        }
        return results

    }

}
