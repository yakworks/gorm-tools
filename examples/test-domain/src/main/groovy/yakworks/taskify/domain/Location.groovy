/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.taskify.domain

import grails.persistence.Entity

@Entity
class Location {
    // address fields
    String street
    String city
    String state //provence
    String zipCode //postalCode
    String country = "US"

    static constraints = {
        // city nullable: false
        // address nullable: true
    }
}
