package gpbench.model

import gpbench.model.basic.CityBasic
import grails.persistence.Entity

@Entity
class Origin {

    Country country
    Region state
    CityBasic city

    static constraints = {
        country nullable: false
        state nullable: true
        city nullable: true
    }
}
