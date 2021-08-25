/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.model.IdEnum

@CompileStatic
enum JobState implements IdEnum<JobState, Integer> {
    Failed(0), Success(1), InProcess(2), Void(3)
    Integer id

    JobState(Integer id) {
        this.id = id
    }
}
