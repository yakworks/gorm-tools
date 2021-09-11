/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoUtil
import yakworks.rally.common.LinkXRefRepo
import yakworks.rally.common.LinkXRefTrait

/**
 * common trait that a concrete composite entity can implement if the stock AttachmentLink will not suffice
 * for example, Org has its own OrgAttachment
 *
 * @param <X> the LinkXRef entity
 */
@CompileStatic
trait AttachmentLinkTrait<X> implements LinkXRefTrait {

    abstract Attachment getAttachment()
    abstract void setAttachment(Attachment t)

    abstract Serializable getAssociationId(String param1)

    // used for the equals and hashcode
    @Transient
    Long getAttachmentId() { (Long)this.getAssociationId("attachment") }

    static LinkXRefRepo<X,Attachment> getAttachmentLinkRepo() {
        (LinkXRefRepo<X,Attachment>) RepoUtil.findRepo(this)
    }

    static X create(Persistable entity, Attachment attach, Map args = [:]) {
        getAttachmentLinkRepo().create(entity, attach, args)
    }


    static X get(Persistable entity, Attachment attach) {
        getAttachmentLinkRepo().get(entity, attach)
    }

    static List<X> list(Persistable entity) {
        getAttachmentLinkRepo().queryFor(entity).list()
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
        if (other instanceof AttachmentLinkTrait<X>) {
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
