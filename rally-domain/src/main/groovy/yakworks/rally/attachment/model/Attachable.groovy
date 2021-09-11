/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoUtil
import yakworks.commons.lang.Validate
import yakworks.rally.attachment.repo.AttachmentLinkRepo

@CompileStatic
trait Attachable {

    List<Attachment> getAttachments() {
        getAttachmentLinkRepo().listItems((Persistable)this)
    }

    boolean hasAttachments() {
        return getAttachmentLinkRepo().exists((Persistable)this)
    }

    AttachmentLink addAttachment(Attachment attach) {
        def entity = (Persistable)this
        Validate.notNull(entity.id, "[entity.id]")
        return getAttachmentLinkRepo().create(entity, attach)
    }

    AttachmentLinkRepo getAttachmentLinkRepo() {
        (AttachmentLinkRepo) RepoUtil.findRepo(AttachmentLink)
    }

    static constraintsMap = [
        attachments: [ description: 'the attachments for this item', validate: false]
        //hasAttachments: [ d: 'true if this has attachments', nullable: true]
    ]
}
