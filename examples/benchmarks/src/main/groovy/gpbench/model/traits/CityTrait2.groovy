package gpbench.model.traits


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gpbench.model.Country
import gpbench.model.Region

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

    @CompileDynamic
    static CityTrait2Constraints(Object delegate) {
        def c = {
            name2 blank: false, nullable: false
            shortCode2 blank: false, nullable: false
            latitude2 nullable: false, scale: 4, max: 90.00
            longitude2 nullable: false, scale: 4, max: 380.00
            region2 nullable: false
            country2 nullable: false
            state2 nullable: true
            countryName2 nullable: true
        }
        c.delegate = delegate
        c()
    }
}
