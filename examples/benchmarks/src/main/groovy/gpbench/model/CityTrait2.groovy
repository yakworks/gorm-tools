package gpbench.model

import gpbench.Country
import gpbench.Region
import groovy.transform.CompileStatic

@CompileStatic
trait CityTrait2 {
    String name2
    String shortCode2
    String state2
    String countryName2

    BigDecimal latitude2
    BigDecimal longitude2

    Region region2
    Country country2
}

class CityTrait2Constraints implements CityTrait2{

    static constraints = {
        name2 blank: false, nullable: false
        shortCode2 blank: false, nullable: false
        latitude2 nullable: false, scale: 4, max: 90.00
        longitude2 nullable: false, scale: 4, max: 380.00
        region2 nullable: false
        country2 nullable: false
        state2 nullable: true
        countryName2 nullable: true
    }
}
