package gpbench.model

import gpbench.Country
import gpbench.Region
import groovy.transform.CompileStatic

@CompileStatic
trait CityTrait3 {
    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

    Region region3
    Country country3
}

class CityTrait3Constraints implements CityTrait3{

    static constraints = {
        name3 blank: false, nullable: false
        shortCode3 blank: false, nullable: false
        latitude3 nullable: false, scale: 4, max: 90.00
        longitude3 nullable: false, scale: 4, max: 380.00
        region3 nullable: false
        country3 nullable: false
        state3 nullable: true
        countryName3 nullable: true
    }
}
