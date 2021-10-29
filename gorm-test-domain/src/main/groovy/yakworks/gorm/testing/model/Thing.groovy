/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.repository.model.GormRepoEntity
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
class Thing implements GormRepoEntity<Thing, ThingRepo> {
    static List qSearchIncludes = ['country', 'city']
    // address fields
    String street
    String city
    String country = "US"

    static constraints = {
        // city nullable: false
        // address nullable: true
        country maxSize:2
    }
}