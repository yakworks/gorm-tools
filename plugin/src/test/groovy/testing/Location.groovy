package testing

import grails.persistence.Entity

@Entity
class Location{
    int id
    String city
    Nested nested
}
