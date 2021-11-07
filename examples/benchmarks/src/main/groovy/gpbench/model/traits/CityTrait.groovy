package gpbench.model.traits


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

    // Region region
    // Country country

    static constraintsMap = [
        name:[ d: 'The full name for this entity', nullable: false],
        shortCode:[ d: 'The full name for this entity', nullable: false],
        latitude:[ d: 'The full name for this entity', nullable: false, scale: 4, max: 90.00],
        longitude:[ d: 'The full name for this entity', nullable: false, scale: 4, max: 380.00],
        state:[ d: 'The full name for this entity'],
        countryName:[ d: 'The full name for this entity']
    ]

}
