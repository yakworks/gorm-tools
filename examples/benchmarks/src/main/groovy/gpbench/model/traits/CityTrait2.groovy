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

    // Region region2
    // Country country2

    static constraintsMap = [
        name2:[ d: 'fff', blank: false, nullable: false],
        shortCode2:[ d: 'fff', blank: false, nullable: false],
        latitude2:[ d: 'fff', nullable: false, scale: 4, max: 90.00],
        longitude2:[ d: 'fff', nullable: false, scale: 4, max: 380.00],
        state2:[ d: 'fff'],
        countryName2:[ d: 'fff']
    ]
}
