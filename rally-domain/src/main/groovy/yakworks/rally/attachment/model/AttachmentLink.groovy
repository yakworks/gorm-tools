/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.attachment.repo.AttachmentLinkRepo
import yakworks.rally.common.LinkXRefTrait

/**
 * generalized composite table to link a Attachment to any entity
 */
@Entity
@GrailsCompileStatic
class AttachmentLink implements LinkXRefTrait, GormRepoEntity<AttachmentLink, AttachmentLinkRepo>, Serializable {
    static belongsTo = [attachment: Attachment]

    static mapping = {
        id composite: ['attachment', 'linkedId', 'linkedEntity']
        version false
        attachment column: 'attachmentId', fetch: 'join'
    }

    static AttachmentLinkRepo getAttachmentLinkRepo() {
        RepoUtil.findRepo(this) as AttachmentLinkRepo
    }

    static AttachmentLink create(Persistable linkedEntity, Attachment attach, Map args = [:]) {
        getAttachmentLinkRepo().create(linkedEntity, attach, args)
    }

    static AttachmentLink get(Persistable linkedEntity, Attachment attach) {
        getAttachmentLinkRepo().get(linkedEntity, attach)
    }

    static List<AttachmentLink> list(Persistable linkedEntity) {
        getAttachmentLinkRepo().list(linkedEntity)
    }

    static List<AttachmentLink> list(Attachment attach) {
        getAttachmentLinkRepo().list(attach)
    }

    static List<Attachment> listAttachments(Persistable entity) {
        getAttachmentLinkRepo().listItems(entity)
    }

    static boolean exists(Persistable entity, Attachment attach) {
        getAttachmentLinkRepo().exists(entity, attach)
    }

    static boolean hasAttachments(Persistable entity) {
        getAttachmentLinkRepo().exists(entity)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof AttachmentLink) {
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
