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
import grails.gorm.transactions.Transactional
import jakarta.annotation.Nullable
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
import yakworks.security.SecService

/**
 * WIP
 * Moved the mass stuff out of repo for now to it does not polute it.
 */
@Service @Lazy
@Slf4j
@CompileStatic
class ActivityBulk {

    @Autowired @Nullable
    ActivityRepo activityRepo

    @Autowired @Nullable
    ActivityLinkRepo activityLinkRepo

    @Autowired @Nullable
    AttachmentRepo attachmentRepo

    @Autowired @Nullable
    ProblemHandler problemHandler

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
            if (createdActivities[org.getId()] && entityName != "Payment") {
                activity = createdActivities[org.getId()]
            } else {
                List copiedAttachments = attachments
                //Here !=0 = for first payment use the original attachments and for all rest of the payments copy it.
                //so same attachments are not shared between payments.
                if (i != 0 && newAttachments) {
                    copiedAttachments = attachments.collect { attachmentRepo.copy(it as Attachment)}
                }
                activity = createActivity(activityData.name.toString(), org, (Map) activityData.task, copiedAttachments, entityName, source)
                createdActivities[org.getId() as Long] = activity
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
    //FIXME this is old and should probably be deprected, currentyl used in insertMassActivity
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
