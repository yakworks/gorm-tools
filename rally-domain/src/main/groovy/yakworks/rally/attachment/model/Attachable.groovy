/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.core.GenericTypeResolver

import gorm.tools.model.Persistable
import yakworks.rally.attachment.repo.AttachmentLinkRepo

@CompileStatic
trait Attachable<D> {

    List<Attachment> getAttachments() {
        getAttachmentLinkRepo().listAttachments(this as Persistable)
    }

    boolean hasAttachment(Attachment attachment) {
        return getAttachmentLinkRepo().exists(this as Persistable, attachment)
    }

    Class<D> getAttachmentLinkClass() {
        (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), Attachable)
    }

    @SuppressWarnings(['FieldName'])
    private static AttachmentLinkRepo _attachmentLinkRepo

    AttachmentLinkRepo getAttachmentLinkRepo() {
        if (!_attachmentLinkRepo) this._attachmentLinkRepo = ClassPropertyFetcher.getStaticPropertyValue(getAttachmentLinkClass(), 'repo', AttachmentLinkRepo)
        return _attachmentLinkRepo
    }
}
