/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package testing

import gorm.tools.job.JobTrait
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@IdEqualsHashCode
@Entity
@GrailsCompileStatic
class JobImpl implements JobTrait, RepoEntity<JobImpl> {
    String message  // not sure if needed
    byte[] data  // data we are getting
    // String fileWithJson  // option if json is too big
    byte[] results

    // int persistenceDuration  //job can be purged after that time (number of days???)

    private static final int MAX_MEG_IN_BYTES = 1024 * 1024 * 10 //10 megabytes

    static constraintsMap = [
        state:[ d: 'State of the job', nullable: false],
        message:[ d: 'Main message from results'],
        data:[ d: 'Json data that is passed in, for example list of items to bulk create', maxSize: MAX_MEG_IN_BYTES],
        results: [d: 'Json list of results', maxSize: MAX_MEG_IN_BYTES]
    ]

    static mapping = {
        //id generator: 'assigned'
    }
}
