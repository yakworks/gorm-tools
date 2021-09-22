package gpbench.model.traits


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gpbench.model.Country
import gpbench.model.Region

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

    @CompileDynamic
    static CityTrait3Constraints(Object delegate) {
        def c = {
            name3 blank: false, nullable: false
            shortCode3 blank: false, nullable: false
            latitude3 nullable: false, scale: 4, max: 90.00
            longitude3 nullable: false, scale: 4, max: 380.00
            region3 nullable: false
            country3 nullable: false
            state3 nullable: true
            countryName3 nullable: true
        }
        c.delegate = delegate
        c()
    }
}
