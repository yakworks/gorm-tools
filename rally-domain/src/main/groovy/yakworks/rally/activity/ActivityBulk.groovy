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

import gorm.tools.model.Persistable
import gorm.tools.model.SourceType
import gorm.tools.problem.ProblemHandler
import gorm.tools.repository.RepoLookup
import gorm.tools.utils.GormMetaUtils
import grails.gorm.transactions.Transactional
import yakworks.commons.map.Maps
import yakworks.rally.activity.model.Activity
import yakworks.rally.activity.repo.ActivityLinkRepo
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.model.Attachment
import yakworks.rally.attachment.repo.AttachmentLinkRepo
import yakworks.rally.attachment.repo.AttachmentRepo
import yakworks.rally.orgs.model.Org

import static yakworks.commons.beans.Transform.objectListToIdMapList

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

    @Autowired AttachmentLinkRepo attachmentLinkRepo

    @Autowired ProblemHandler problemHandler

    /** Load entities by id and create mass activities (used by MassUpdateService). */
    void createActivities(Class entityClass, List ids, Map activityData, boolean linkTargets) {
        if (!ids || !activityData) return
        List targets = RepoLookup.findRepo(entityClass).getAll(ids).findAll { it != null } as List
        insertMassActivity(targets, activityData, null, linkTargets)
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
     * Creates one Activity per org for the given targets via ActivityRepo.create.
     * Attachments are created once and linked to each activity (no copies).
     * Tags, note, and task are handled by ActivityRepo.
     *
     * Activity links are created only if linkTargets=true (eg for ArTran/Payment etc)
     *
     * @param targets entities with getOrg() / org (Customer, CustAccount, ArTran, Payment)
     * @param activityData
     * @param Activity source source optional (activity.source is set from entity name)
     * @param linkTargets true for ArTran/Payment (creates ActivityLinks); false for Customer/CustAccount
     */
    @Transactional
    List<Activity> insertMassActivity(List targets, Map activityData, String source = null, boolean linkTargets = false) {
        if (!targets) return [] as List<Activity>

        // create attachments once; each activity gets AttachmentLinks to the same attachments
        List<Map> attachmentLinkData = null
        List attachmentData = activityData.remove('attachments') as List
        if (attachmentData) {
            List<Attachment> attachments = attachmentRepo.createOrUpdate(attachmentData) as List<Attachment>
            attachmentLinkData = objectListToIdMapList(attachments)
        }

        // base data for ActivityRepo.create — tags/task/note go through activity repo
        Map baseData = Maps.clone(activityData)

        Map<Long, Activity> byOrg = [:]

        // unwrap Hibernate proxy so linkedEntity/source is e.g. Customer not Customer$HibernateProxy$…
        String entityName = GormMetaUtils.unwrapIfProxy(targets[0].getClass().simpleName)

        for (Object target : targets) {
            Org org = target['org'] as Org
            Activity activity = byOrg[org.id]
            if (!activity) {
                Map data = Maps.clone(baseData)
                data.org = org
                data.source = source ?: entityName
                data.sourceType = SourceType.App
                activity = activityRepo.create(data)
                // link pre-created attachments (same pattern as ActivityRepo.doAttachments)
                if (attachmentLinkData) {
                    attachmentLinkRepo.addOrRemove((Persistable) activity, attachmentLinkData)
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

}
