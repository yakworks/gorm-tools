/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.validation.Rejector

@GormRepository
@CompileStatic
class ThingRepo extends LongIdGormRepo<Thing> {

    @RepoListener
    void beforeValidate(Thing thing, Errors errors) {
        //test rejectValue
        if(thing.name == 'RejectThis'){
            Rejector.of(thing, errors).withError('name', 'no.from.ThingRepo')
        }
    }
}
