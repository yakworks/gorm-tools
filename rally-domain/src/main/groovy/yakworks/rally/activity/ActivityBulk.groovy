/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.model.SourceType
import gorm.tools.problem.ProblemHandler
import gorm.tools.utils.GormMetaUtils
import grails.gorm.transactions.Transactional
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.activity.repo.ActivityLinkRepo
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink

/**
 * WIP
 * Moved the mass stuff out of repo for now to it does not polute it.
 */
@Service @Lazy
@Slf4j
@CompileStatic
class ActivityBulk {

    @Autowired ActivityRepo activityRepo

    @Autowired ActivityLinkRepo activityLinkRepo

    @Autowired AttachmentRepo attachmentRepo

    @Autowired ProblemHandler problemHandler

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
        activityRepo.addNote(activity, body)
        activityRepo.updateNameSummary(activity)

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
     * Creates one Activity per org for the given targets.
     * Attachments are created once and linked to each activity (no copies).
     *
     * @param targets entities with getOrg() / org (Customer, CustAccount, ArTran, Payment)
     * @param activityData name, optional task, optional attachments
     * @param source unused for now (activity.source is set from entity name)
     * @param linkTargets true for ArTran/Payment (ActivityLinks); false for Customer/CustAccount (org on activity is enough)
     */
    @Transactional
    List<Activity> insertMassActivity(List targets, Map activityData, String source = null, boolean linkTargets = false) {
        if (!targets) return [] as List<Activity>

        // create attachments once; each activity gets AttachmentLinks to the same rows
        List<Attachment> attachments = [] as List<Attachment>
        List attachmentData = activityData?.attachments as List
        if (attachmentData) {
            attachments = attachmentRepo.createOrUpdate(attachmentData) as List<Attachment>
        }

        Map<Long, Activity> byOrg = [:]
        // unwrap Hibernate proxy so linkedEntity/source is e.g. Customer not Customer$HibernateProxy$…
        String entityName = GormMetaUtils.unwrapIfProxy(targets[0].getClass().simpleName)

        for (Object target : targets) {
            Org org = target['org'] as Org
            Activity activity = byOrg[org.id]
            if (!activity) {
                activity = createActivity(activityData.name.toString(), org, (Map) activityData.task, attachments, entityName, source)
                if (activityData.tags) {
                    TagLink.addOrRemoveTags(activity, activityData.tags)
                }
                byOrg[org.id] = activity
            }
            // ArTran/Payment share an org — link each target; Customer/CustAccount skip (unique org)
            if (linkTargets) {
                activityLinkRepo.create(target['id'] as Long, entityName, activity)
            }
        }

        return new ArrayList<Activity>(byOrg.values())
    }

    /** Builds a note or task activity and links the given attachments. */
    @Transactional
    Activity createActivity(String text, Org org, Map task, List<Attachment> attachments, String entityName, String source = null) {

        Activity activity = new Activity(
            org         : org,
            name        : text,
            source      : entityName,
            sourceType  : SourceType.App
        )
        activityRepo.generateId(activity)
        if (task) {
            activity.task = createActivityTask(task)
            activity.kind = activity.task.taskType.kind
        } else {
            activityRepo.addNote(activity, text)
            activityRepo.updateNameSummary(activity)
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

}
