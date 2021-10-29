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

@GormRepository
@CompileStatic
class AddressRepo implements GormRepo<Address> {

    @RepoListener
    void beforeValidate(Address loc, Errors errors) {
        //test rejectValue
        if(loc.city == 'AddyVille'){
            rejectValue(loc, errors, 'city', loc.city, 'no.AddyVilles')
        }
    }
}
