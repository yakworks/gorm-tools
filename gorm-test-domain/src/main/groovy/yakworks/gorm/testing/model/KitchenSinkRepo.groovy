/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional

@GormRepository
@CompileStatic
class KitchenSinkRepo implements GormRepo<KitchenSink> {

    @RepoListener
    void beforeValidate(KitchenSink o) {
        o.beforeValidateCheck = "got it"
        //test rejectValue
        if(o.location?.street == 'OrgRepoStreet'){
            rejectValue(o, 'location.street', o.location.street, 'from.OrgRepo')
        }
        if(o.name == 'foos'){
            rejectValue(o, 'name', o.name, 'no.foos')
        }
    }

    @RepoListener
    void beforePersist(KitchenSink o, BeforePersistEvent e) {
        if(!o.kind) o.kind = KitchenSink.Kind.CLIENT
    }


    @Transactional
    KitchenSink inactivate(Long id) {
        KitchenSink o = KitchenSink.get(id)
        o.inactive = true
        o.persist()
        o
    }
}
