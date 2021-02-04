/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoUtil
import yakworks.rally.attachment.repo.AttachmentLinkRepo

@CompileStatic
trait Attachable {

    List<Attachment> getAttachments() {
        getAttachmentLinkRepo().listItems(this as Persistable)
    }

    boolean hasAttachment(Attachment attachment) {
        return getAttachmentLinkRepo().exists(this as Persistable, attachment)
    }

    @SuppressWarnings(['FieldName'])
    private static AttachmentLinkRepo _attachmentLinkRepo

    AttachmentLinkRepo getAttachmentLinkRepo() {
        if (!_attachmentLinkRepo) this._attachmentLinkRepo = RepoUtil.findRepo(AttachmentLink) as AttachmentLinkRepo
        return _attachmentLinkRepo
    }
}
