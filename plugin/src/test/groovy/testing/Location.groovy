package testing

import grails.persistence.Entity

@Entity
class Location {
    String city
    Nested nested

    static constraints = {
        nested nullable: false
        city nullable: false
    }
}
