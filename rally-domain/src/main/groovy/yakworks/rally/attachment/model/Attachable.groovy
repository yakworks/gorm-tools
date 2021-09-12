/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable

@CompileStatic
trait Attachable {

    @Transient
    int _hasAttachments = 0

    List<Attachment> getAttachments() {
        AttachmentLink.listAttachments((Persistable)this)
    }

    int hasAttachments() {
        if(!_hasAttachments) _hasAttachments = (Integer)AttachmentLink.repo.queryFor((Persistable)this).count()
        return _hasAttachments
    }

    AttachmentLink addAttachment(Attachment attach) {
        def al = AttachmentLink.create((Persistable)this, attach)
        _hasAttachments = _hasAttachments + 1
        return al
    }

    static constraintsMap = [
        attachments: [ description: 'the attachments for this item', validate: false]
        //hasAttachments: [ d: 'true if this has attachments', nullable: true]
    ]
}
