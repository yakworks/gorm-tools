/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.attachment

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.core.GenericTypeResolver

import yakworks.module.attachment.entity.Attachment

@CompileStatic
trait Attachable<D> {

    abstract Long getId()

    List<Attachment> getTags() {
        getEntityAttachmentRepo().listAttachments(getId())
    }

    boolean hasTag(Attachment attachment) {
        return getEntityAttachmentRepo().exists(getId(), attachment)
    }

    Class<D> getEntityTagClass() {
        (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), Attachable)
    }

    @SuppressWarnings(['FieldName'])
    private static EntityAttachmentRepoTrait _entityAttachmentRepo

    EntityAttachmentRepoTrait getEntityAttachmentRepo() {
        if (!_entityAttachmentRepo) this._entityAttachmentRepo = ClassPropertyFetcher.getStaticPropertyValue(getEntityTagClass(), 'repo', EntityAttachmentRepoTrait)
        return _entityAttachmentRepo
    }
}
