package gpbench.model.traits


import groovy.transform.CompileStatic

@CompileStatic
trait CityTrait3 {
    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

    // Region region3
    // Country country3

    static constraintsMap = [
        name3:[ d: 'The full name for this entity', nullable: false],
        shortCode3:[ d: 'The full name for this entity', nullable: false],
        latitude3:[ d: 'The full name for this entity', nullable: false, scale: 4, max: 90.00],
        longitude3:[ d: 'The full name for this entity', nullable: false, scale: 4, max: 380.00],
        state3:[ d: 'The full name for this entity'],
        countryName3:[ d: 'The full name for this entity']
    ]
}
