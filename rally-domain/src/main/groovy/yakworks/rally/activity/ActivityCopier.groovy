/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity

import javax.inject.Inject

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.utils.GormUtils
import grails.gorm.transactions.Transactional
import jakarta.annotation.Nullable
import yakworks.api.ApiResults
import yakworks.api.Result
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.model.ActivityContact
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.activity.model.Task
import yakworks.rally.activity.repo.ActivityLinkRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.model.AttachmentLink
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org
import yakworks.rally.tag.model.TagLink

@Service @Lazy
@Slf4j
@CompileStatic
class ActivityCopier extends LongIdGormRepo<Activity> {

    @Inject @Nullable
    ActivityLinkRepo activityLinkRepo

    @Inject @Nullable
    AttachmentRepo attachmentRepo

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
            } catch (e) {
                results << problemHandler.handleException(e)
            }
        }
        return results

    }

}
