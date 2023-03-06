/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity

import java.time.LocalDateTime
import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.commons.lang3.StringUtils
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Transactional
import jakarta.annotation.Nullable
import yakworks.api.Result
import yakworks.api.problem.Problem
import yakworks.api.problem.data.NotFoundProblem
import yakworks.commons.lang.Validate
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityLink
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.model.TaskStatus
import yakworks.rally.activity.model.TaskType
import yakworks.rally.mail.MailMessageSender
import yakworks.rally.mail.model.MailMessage
import yakworks.rally.orgs.model.Org
import yakworks.spring.AppCtx

/**
 * Functions to support Activities.
 */
@Service @Lazy
@Slf4j
@CompileStatic
class ActivityService {

    @Inject @Nullable MailMessageSender mailMessageSender

    /** static helper for ActivityUtils */
    static ActivityService bean(){
        AppCtx.get('activityService', ActivityService)
    }

    /**
     * quick way to create and Informational message that occured
     */
    @Transactional
    Activity createLog(Long orgId, String message){
        def params = [kind: Activity.Kind.Log, orgId: orgId, name: message]
        Activity activity = Activity.create(params)
        return activity
    }

    /**
     * quick easy way to create a Note
     */
    @Transactional
    Activity createNote(Long orgId, String note){
        def params = [orgId: orgId, note:[body: 'foo'], linkedId: 1, linkedEntity:'Contact']
        Activity activity = Activity.create(params)
        return activity
    }

    /** Builds an email but does not save */
    @Transactional
    Activity buildEmail(Long orgId, MailMessage mailMessage){
        Activity activity = new Activity(kind: Activity.Kind.Email, org: Org.load(orgId))
        if (mailMessage.subject?.length() > 255) {
            activity.name = StringUtils.abbreviate(mailMessage.subject, 255)
        } else {
            activity.name = mailMessage.subject
        }
        activity.mailMessage = mailMessage
        return activity
    }

    /**
     * quick easy way to create a Todo activity
     */
    @Transactional
    Activity createTodo(Org org, Long userId, String name, String linkedEntity = null,
                        List<Long> linkedIds = null, LocalDateTime dueDate = LocalDateTime.now()) {

        Activity activity = Activity.repo.create(org: org, name: name, kind : Activity.Kind.Todo)

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

    @Transactional
    void completeTask(Task task, Long completedById) {
        Validate.notNull(completedById, "[completedById]")
        task.bind(status: TaskStatus.COMPLETE,
            state: TaskStatus.COMPLETE.id as Integer,
            completedDate: LocalDateTime.now(),
            completedBy: completedById)
    }

    /**
     * Sends the email with mailMessageSender. Update act to leve:Error if anything goes wrong.
     * Will update the mailMessage to sent via mailMessageSender.send
     * Throws NotFoundPropblem is the activity doesn't have a mailMessage, or throws if persist fails.<br>
     * So this needs to wrap in try catch as well as check the result for status.
     * @param actId the id of the activity
     * @return the activity it operated on
     * @throw NotFoundProblem if it not found
     */
    @Transactional
    Result sendEmail(Long actId){
        Activity act = Activity.get(actId)
        MailMessage msg = act?.mailMessage
        //safety check to throw notFound if either act or mailMessage are null.
        RepoUtil.checkFound(msg, actId, 'Activity MailMessage')

        Result result = mailMessageSender.send(msg)
        if(result instanceof Problem){
            act.level = Activity.AlertLevel.Error
            act.persist()
        }
        return result
    }
}
