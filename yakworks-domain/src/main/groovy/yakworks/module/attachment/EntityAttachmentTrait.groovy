/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.attachment

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import yakworks.module.attachment.entity.Attachment

@CompileStatic
trait EntityAttachmentTrait<D> implements PersistableRepoEntity<D, GormRepo<D>>, QueryMangoEntity<D> {

    Long linkedId

    abstract Attachment getAttachment()
    abstract void setAttachment(Attachment t)

    abstract Serializable getAssociationId(String param1)

    @Transient
    Long getAttachmentId() { (Long)this.getAssociationId("attachment") }

    static EntityAttachmentRepoTrait<D> getEntityAttachmentRepo() {
        getRepo() as EntityAttachmentRepoTrait<D>
    }

    static D create(Persistable entity, Attachment theAttachment, Map args = [:]) {
        getEntityAttachmentRepo().create(entity, theAttachment, args)
    }

    static D create(long entityId, Attachment theAttachment, Map args = [:]) {
        getEntityAttachmentRepo().create(entityId, theAttachment, args)
    }

    static D get(long lid, Attachment theAttachment) {
        getEntityAttachmentRepo().get(lid, theAttachment)
    }

    static D get(long lid, long tagId) {
        get(lid, Attachment.load(tagId))
    }

    static List<D> list(long lid) {
        getEntityAttachmentRepo().list(lid)
    }

    static List<Attachment> listAttachments(long lid) {
        getEntityAttachmentRepo().listAttachments(lid)
    }

    static boolean exists(long linkedId, Attachment theAttachment) {
        getEntityAttachmentRepo().exists(linkedId, theAttachment)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof EntityAttachmentTrait<D>) {
            return other.getLinkedId() == getLinkedId() && other.getAttachmentId() == getAttachmentId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getLinkedId()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedId()) }
        if (getAttachmentId()) { hashCode = HashCodeHelper.updateHash(hashCode, getAttachmentId()) }
        hashCode
    }


}
