/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.attachment.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@Entity
@GrailsCompileStatic
class FileData implements RepoEntity<FileData>, Serializable {
    private static final int MAX_MEG_IN_BYTES = 1024 * 1024 * 5 //5 megabytes
    static belongsTo = [attachment: Attachment]
    byte[] data

    static mapping = {
        table 'FileData'
    }

    static constraints = {
        data(nullable: false, minSize: 1, maxSize: MAX_MEG_IN_BYTES)
    }
}
