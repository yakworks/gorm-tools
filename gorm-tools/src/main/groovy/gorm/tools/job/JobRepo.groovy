/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import groovy.transform.CompileStatic


@CompileStatic
trait JobRepo implements GormRepo<JobTrait> {

    void createJob(String uriPath, Map body) {
        job = Job.repo.create(params)
    //
    }
}


