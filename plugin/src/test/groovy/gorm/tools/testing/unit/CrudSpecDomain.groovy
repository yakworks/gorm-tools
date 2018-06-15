/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.testing.unit

import gorm.tools.compiler.GormRepository
import gorm.tools.repository.GormRepo
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
    void beforePersist(CrudSpecDomain entity, Map args) {
        if(entity.firstName || entity.lastName) {
            entity.name = (entity.firstName + ' ' + (entity.lastName?:'')).trim()
        }
    }
}
