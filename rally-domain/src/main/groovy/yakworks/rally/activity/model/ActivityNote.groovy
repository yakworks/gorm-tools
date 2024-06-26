/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.audit.AuditStamp

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class ActivityNote implements RepoEntity<ActivityNote>, Serializable {
    static belongsTo = [Activity]

    String body // the note body
    String contentType = 'plain' //plain,html or markdown

    static mapping = {
        id generator: 'assigned'
        body sqlType:'TEXT'
    }

    static constraintsMap = [
        body:[d: 'The note body', nullable: false, maxSize: 65535],
        contentType:[d: 'plain, html, markdown', nullable: false, default: 'plain']
    ]
}
