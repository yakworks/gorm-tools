/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import groovy.transform.CompileStatic

import org.springframework.validation.Errors

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo

@GormRepository
@CompileStatic
class ThingRepo implements GormRepo<Thing>, IdGeneratorRepo {

    @RepoListener
    void beforeValidate(Thing thing, Errors errors) {
        generateId(thing)
        //test rejectValue
        if(thing.city == 'RejectThis'){
            rejectValue(thing, errors, 'city', thing.city, 'no.from.ThingRepo')
        }
    }
}
