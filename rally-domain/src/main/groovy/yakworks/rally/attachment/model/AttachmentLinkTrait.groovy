/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import yakworks.rally.common.LinkedEntityRepoTrait

/**
 * common trait that a concrete composite entity can implement if the stock AttachmentLink will not suffice
 * for example, Org has its own OrgAttachment
 */
@CompileStatic
trait AttachmentLinkTrait<D> implements PersistableRepoEntity<D, GormRepo<D>, Long>, QueryMangoEntity<D> {

    Long linkedId
    String linkedEntity

    abstract Attachment getAttachment()
    abstract void setAttachment(Attachment t)

    abstract Serializable getAssociationId(String param1)

    // used for the equals and hashcode
    @Transient
    Long getAttachmentId() { (Long)this.getAssociationId("tag") }

    static LinkedEntityRepoTrait<D,Attachment> getAttachmentLinkRepo() {
        getRepo() as LinkedEntityRepoTrait<D,Attachment>
    }

    static D create(Persistable entity, Attachment attach, Map args = [:]) {
        getAttachmentLinkRepo().create(entity, attach, args)
    }


    static D get(Persistable entity, Attachment attach) {
        getAttachmentLinkRepo().get(entity, attach)
    }

    static List<D> list(Persistable entity) {
        getAttachmentLinkRepo().list(entity)
    }

    static List<Attachment> listAttachments(Persistable entity) {
        getAttachmentLinkRepo().listItems(entity)
    }

    static boolean exists(Persistable entity, Attachment attach) {
        getAttachmentLinkRepo().exists(entity, attach)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof AttachmentLinkTrait<D>) {
            return other.getLinkedId() == getLinkedId() && other.getLinkedEntity() == getLinkedEntity() && other.getAttachmentId() == getAttachmentId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getLinkedId()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedId()) }
        if (getLinkedEntity()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedEntity()) }
        if (getAttachmentId()) { hashCode = HashCodeHelper.updateHash(hashCode, getAttachmentId()) }
        hashCode
    }


}
