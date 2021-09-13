/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.LinkedEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.attachment.repo.AttachmentLinkRepo

/**
 * generalized composite table to link a Attachment to any entity
 */
@Entity
@GrailsCompileStatic
class AttachmentLink implements LinkedEntity, GormRepoEntity<AttachmentLink, AttachmentLinkRepo>, Serializable {
    static belongsTo = [attachment: Attachment]

    static mapping = {
        id composite: ['attachment', 'linkedId', 'linkedEntity']
        version false
        attachment column: 'attachmentId', fetch: 'join'
    }

    static AttachmentLink create(Persistable linkedEntity, Attachment attach, Map args = [:]) {
        getRepo().create(linkedEntity, attach, args)
    }

    static AttachmentLink get(Persistable linkedEntity, Attachment attach) {
        getRepo().get(linkedEntity, attach)
    }

    static List<AttachmentLink> list(Persistable entity) {
        getRepo().list(entity)
    }

    static List<Attachment> listAttachments(Persistable entity) {
        getRepo().listAttachments(entity)
    }

    static boolean exists(Persistable entity, Attachment attach) {
        getRepo().exists(entity, attach)
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
