/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.job

import gorm.tools.audit.AuditStamp
import gorm.tools.job.JobTrait
import grails.compiler.GrailsCompileStatic
import grails.gorm.annotation.Entity

/** An instance created right away when "any job" in 9ci is called. Either called through restApi from outside, scheduled job in quartz or
 * manually started job (Look at SourceTrait)
 * Job may no longer exist to query. 9ci only logs the last 100 jobs. Jobs also expire within an hour. */
@Entity
@AuditStamp
@GrailsCompileStatic
class Job implements JobTrait<Job>, Serializable {

    String message  // not sure if needed

    // String fileWithJson  // option if json is too big

    //The "results" is a response of resources that were successfully and unsuccessfully updated or created after processing.
    // The results differ depending on the sourceType of the job
    byte[] data

    // int persistenceDuration  //job can be purged after that time (number of days???)

    private static final int MAX_MEG_IN_BYTES = 1024 * 1024 * 10 //10 megabytes

    static mapping = {
        state column: 'state', lazy: false, enumType: 'identity'
    }

    static constraintsMap = [
        state:[ d: 'State of the job', nullable: false],
        message:[ d: 'Main message from results'],
        requestData:[ d: 'Json data (stored as byte array) that is passed in, for example list of items to bulk create', maxSize: MAX_MEG_IN_BYTES],
        data: [d: 'Json list of results', maxSize: MAX_MEG_IN_BYTES]
    ]



}
