/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.unit

import gorm.tools.repository.GormRepository
import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.persistence.Entity

@Entity
class CrudSpecDomain {
    String name
    String firstName
    String lastName
    CrudSpecAssocDomain assoc

    static constraints = {
        firstName nullable: false
        name nullable: false
        assoc nullable: false
    }
}

@Entity
class CrudSpecAssocDomain {
    String name

    static constraints = {
        name nullable: false
    }
}

@GormRepository
class CrudSpecDomainRepo implements GormRepo<CrudSpecDomain> {

    @RepoListener
    void beforePersist(CrudSpecDomain entity, BeforePersistEvent e) {
        if(entity.firstName || entity.lastName) {
            entity.name = (entity.firstName + ' ' + (entity.lastName?:'')).trim()
        }
    }
}
