/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.Persistable
import gorm.tools.repository.events.RepositoryEvent
import yakworks.rally.attachment.model.Attachable
import yakworks.rally.attachment.model.AttachmentLink

/**
 * Basic helpers to keep
 */
@CompileStatic
trait AttachableRepoSupport {

    @Autowired(required = false)
    AttachmentLinkRepo attachmentLinkRepo

    @Autowired(required = false)
    AttachmentRepo attachmentRepo

    // call in beforeRemove
    void removeAttachmentLinks(Persistable linkedEntity) {
        attachmentLinkRepo.removeAll(linkedEntity)
    }

    // call in afterPersist
    void addOrRemoveAttachments(Persistable attachable, Object itemParams) {
        // List attachments = attachmentRepo.bulkCreateOrUpdate(itemParams as List)

        List<AttachmentLink> attLinks = attachmentLinkRepo.addOrRemove(attachable, itemParams)

        // update the has attachments
        if(attLinks && attachable instanceof Attachable){
            def attachableEntity = (Attachable)attachable
            attachableEntity._hasAttachments = attLinks.size()
        }
    }

    // call in afterPersist
    void addOrRemoveAttachments(Persistable linkedEntity, RepositoryEvent e) {
        if (e.bindAction && e.data?.attachments){
            addOrRemoveAttachments(linkedEntity, e.data.tags)
        }
    }
}
