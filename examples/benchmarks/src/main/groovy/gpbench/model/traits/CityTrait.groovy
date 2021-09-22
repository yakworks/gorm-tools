package gpbench.model.traits


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gpbench.model.Country
import gpbench.model.Region

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

    @CompileDynamic
    static CityTraitConstraints(Object delegate) {
        def c = {
            name blank: false, nullable: false
            shortCode blank: false, nullable: false
            latitude nullable: false, scale: 4, max: 90.00
            longitude nullable: false, scale: 4, max: 380.00
            region nullable: false
            country nullable: false
            state nullable: true
            countryName nullable: true
        }
        c.delegate = delegate
        c()
    }
}
