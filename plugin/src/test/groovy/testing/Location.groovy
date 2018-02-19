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
