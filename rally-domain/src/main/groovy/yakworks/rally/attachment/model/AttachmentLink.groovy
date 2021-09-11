/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.rally.attachment.repo.AttachmentLinkRepo

/**
 * generalized composite table to link a Attachment to any entity
 */
@Entity
@GrailsCompileStatic
class AttachmentLink implements AttachmentLinkTrait<AttachmentLink>, GormRepoEntity<AttachmentLink, AttachmentLinkRepo>, Serializable {
    static belongsTo = [attachment: Attachment]

    static mapping = {
        id composite: ['attachment', 'linkedId', 'linkedEntity']
        version false
        attachment column: 'attachmentId', fetch: 'join'
    }

}
