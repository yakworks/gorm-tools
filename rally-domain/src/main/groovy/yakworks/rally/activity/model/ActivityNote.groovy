/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

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
        body:[ description: 'The note body', nullable: false, blank: false],
        contentType:[ description: 'plain, html, markdown', nullable: false, default: 'plain']
    ]
}
