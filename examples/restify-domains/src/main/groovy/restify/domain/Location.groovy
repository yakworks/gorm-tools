/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify.domain

import grails.persistence.Entity

@Entity
class Location {
    String city
    String address
    Nested nested

    static constraints = {
        city nullable: false
        nested nullable: true
        address nullable: true
    }
}
