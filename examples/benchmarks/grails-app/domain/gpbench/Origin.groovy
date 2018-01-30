package gpbench

import gpbench.basic.CityBasic

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
