package gpbench.model

import gpbench.Country
import gpbench.Region
import groovy.transform.CompileStatic

@CompileStatic
trait CityTrait {
    String name
    String shortCode
    String state
    String countryName

    BigDecimal latitude
    BigDecimal longitude

    Region region
    Country country
}

//@GrailsCompileStatic
class CityTraitConstraints implements CityTrait {

    static constraints = {
        name blank: false, nullable: false
        shortCode blank: false, nullable: false
        latitude nullable: false, scale: 4, max: 90.00
        longitude nullable: false, scale: 4, max: 380.00
        region nullable: false
        country nullable: false
        state nullable: true
        countryName nullable: true
    }
}
