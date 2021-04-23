/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import gorm.tools.support.Results
import gorm.tools.utils.GormUtils
import grails.gorm.transactions.Transactional
import yakworks.rally.activity.repo.ActivityRepo
import yakworks.rally.attachment.repo.AttachmentLinkRepo
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.Location
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgCalc
import yakworks.rally.orgs.model.OrgFlex
import yakworks.rally.orgs.model.OrgInfo
import yakworks.rally.orgs.model.OrgMember
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.rally.orgs.repo.OrgTagRepo

@Service @Lazy
@Slf4j
@CompileStatic
class OrgCopier {

    @Autowired ContactRepo contactRepo
    @Autowired OrgTagRepo orgTagRepo
    @Autowired AttachmentLinkRepo attachmentLinkRepo
    @Autowired ActivityRepo activityRepo
    // @Inject AttachmentRepo attachmentRepo
    // @Inject OrgSourceRepo orgSourceRepo

    /**
     * Copies fields from the one Org entity to another, it copies associations as well.
     *
     * @param toOrg a target entity into which data should be copied
     * @param fromOrg  a source entity
     * @return the results object which may contain failures if attachments or activity didn't succeed
     */
    @Transactional
    Results copy(Org fromOrg, Org toOrg) {
        if(fromOrg == null) return null
        List<Results> resultList = [] as List<Results>
        GormUtils.copyDomain(toOrg, fromOrg)
        toOrg.type = fromOrg.type
        toOrg.persist()

        if(fromOrg.location) {
            toOrg.location = GormUtils.copyDomain(Location, fromOrg.location, [org: toOrg]) //create a new location
        }

        if(fromOrg.contact) {
            toOrg.contact = contactRepo.copy(fromOrg.contact, new Contact(org: toOrg))
        }

        toOrg.calc = GormUtils.copyDomain(OrgCalc, fromOrg.calc)
        toOrg.flex = GormUtils.copyDomain(OrgFlex, fromOrg.flex)
        toOrg.info = GormUtils.copyDomain(OrgInfo, fromOrg.info)

        //copy member if not already set - avoid error - A different object with the same identifier value was already associated
        if(toOrg.member == null) toOrg.member = GormUtils.copyDomain(OrgMember, OrgMember.get(fromOrg.memberId as Long), [org:toOrg], false)
        toOrg.persist()

        orgTagRepo.copyToOrg(fromOrg, toOrg)

        resultList << attachmentLinkRepo.copy(fromOrg, toOrg)

        resultList << activityRepo.copyToOrg(fromOrg, toOrg)

        return Results.of(resultList).id(fromOrg.id)

    }

}
