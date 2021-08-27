/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.converters.JSON
import groovy.transform.CompileStatic
import org.apache.commons.io.IOUtils


@CompileStatic
trait JobRepo implements GormRepo<JobTrait> {


    @RepoListener
    void beforePersist(JobTrait j, BeforePersistEvent e) {
        // convert String to byte array
        if(j.source instanceof String) {
            j.source = j.source.getBytes()
        }
        if(j.source instanceof JSON) {
            j.source = j.source.toString().getBytes()
        }
    }
}


