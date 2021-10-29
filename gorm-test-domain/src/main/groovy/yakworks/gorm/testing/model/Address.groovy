/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import gorm.tools.repository.model.RepoEntity
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
class Address implements RepoEntity<Address>{
    static List qSearchIncludes = ['street', 'city']
    // address fields
    String street
    String city
    String state //provence
    String zipCode //postalCode
    String country = "US"

    static constraints = {
        // city nullable: false
        // address nullable: true
        country maxSize:2
    }
}
