/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import groovy.transform.CompileStatic

import gorm.tools.model.Persistable

@SuppressWarnings('FieldName')
@CompileStatic
trait Attachable {

    // cached version so we can avoid multiple hits to db , especially in events
    private Boolean _hasAttachments

    boolean hasAttachments() {
        if(_hasAttachments == null) _hasAttachments = AttachmentLink.repo.queryFor((Persistable)this).count() as Boolean
        return _hasAttachments
    }

    void setHasAttachments(boolean val) {
        this._hasAttachments = val
    }

    List<Attachment> getAttachments() {
        AttachmentLink.listAttachments((Persistable) this)
    }

    AttachmentLink addAttachment(Attachment attach) {
        def al = AttachmentLink.create((Persistable)this, attach)
        _hasAttachments = true
        return al
    }

    List<AttachmentLink> addOrRemoveAttachments(Object itemParams) {
        AttachmentLink.addOrRemove((Persistable)this, itemParams)
        // _hasAttachments = _hasAttachments + 1
    }

    static constraintsMap = [
        attachments: [ description: 'the attachments for this item', validate: false]
        //hasAttachments: [ d: 'true if this has attachments', nullable: true]
    ]
}
