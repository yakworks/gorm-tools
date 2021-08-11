/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.audit.AuditStamp
import gorm.tools.model.IdEnum
import gorm.tools.support.Results
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity
import groovy.transform.CompileStatic


@Entity
@AuditStamp
@GrailsCompileStatic
class Job implements Serializable {

    JobState state
    String message
    String json
    String fileWithJson  // option if json is too big
    Results results

    int persistenceDuration  //job can be purged after that time (number of days???)

    static constraintsMap = [
        state:[ description: 'State of the job', nullable: false],
        message:[ description: 'Main message from results'],
        json:[ description: 'Json that is passed in' ]

    ]

    static mapping = {
        id generator: 'assigned'
    }
}
