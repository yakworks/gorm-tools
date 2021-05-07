/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model


import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.attachment.repo.AttachmentLinkRepo

/**
 * generalized composite table to link a Attachment to any entity
 */
@Entity
@GrailsCompileStatic
class AttachmentLink implements AttachmentLinkTrait<AttachmentLink, AttachmentLinkRepo>, Serializable {
    static belongsTo = [attachment: Attachment]
    String linkedEntity
    Long linkedId

    static mapping = {
        id composite: ['attachment', 'linkedId', 'linkedEntity']
        version false
        attachment column: 'attachmentId', fetch: 'join'
    }

    static constraints = {
        linkedEntity nullable: false, blank: false
        linkedId nullable: false
    }

    static List<AttachmentLink> listByAttachment(Attachment attach) {
        getRepo().listByAttachment(attach)
    }

    static void removeAllByAttachment(Attachment attach) {
        getRepo().removeAllByAttachment(attach)
    }

    static boolean exists(Attachment attach) {
        getRepo().exists(attach)
    }
}
