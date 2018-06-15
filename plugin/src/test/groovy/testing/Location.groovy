/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package testing

import grails.persistence.Entity

@Entity
class Location {
    String address
    Nested nested

    static constraints = {
        nested nullable: false
        address nullable: false
    }
}
