/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.repo

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.model.Persistable
import gorm.tools.repository.events.RepositoryEvent

/**
 * helpers trait to add to a Attachable entity's repo
 */
@CompileStatic
trait AttachableRepoSupport {

    @Autowired AttachmentLinkRepo attachmentLinkRepo
    @Autowired AttachmentRepo attachmentRepo

    // call in beforeRemove
    void removeAttachmentLinks(Persistable linkedEntity) {
        attachmentLinkRepo.remove(linkedEntity)
    }

    // call in afterPersist
    void addOrRemoveAttachments(Persistable attachable, Object itemParams) {
        attachmentLinkRepo.addOrRemove(attachable, itemParams)
    }

    // call in afterPersist
    void addOrRemoveAttachments(Persistable linkedEntity, RepositoryEvent e) {
        if (e.bindAction && e.data?.attachments){
            addOrRemoveAttachments(linkedEntity, e.data.tags)
        }
    }
}
