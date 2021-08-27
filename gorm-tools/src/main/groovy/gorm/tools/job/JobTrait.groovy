/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import gorm.tools.repository.model.RepoEntity
import gorm.tools.source.SourceTrait

@CompileStatic
trait JobTrait<D> implements SourceTrait, PersistableRepoEntity<D, GormRepo<D>> {

    Boolean ok = false // change to TRUE if State.Finished without any issues
    JobState state = JobState.InProcess

}
