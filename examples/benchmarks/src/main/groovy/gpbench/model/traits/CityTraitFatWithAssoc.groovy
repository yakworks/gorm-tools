package gpbench.model.traits

import groovy.transform.CompileStatic

import gpbench.model.Country
import gpbench.model.Region

@CompileStatic
trait CityTraitFatWithAssoc extends CityTraitFat {

    abstract void setRegion(Region r)
    abstract void setCountry(Country c)
    // abstract Country country
    // abstract Region region2
    abstract void setRegion2(Region r)
    abstract void setCountry2(Country c)
    // abstract Country country2
    // abstract Region region3
    abstract void setRegion3(Region r)
    abstract void setCountry3(Country c)
    // abstract Country country3

    void setProps(Map row) {
        StaticSetter.setProps(this, row, true)
    }

    static constraintsMap = [
        region:[ d: 'The full name for this entity', nullable: false],
        country:[ d: 'The full name for this entity', nullable: false],
        region2:[ d: 'The full name for this entity', nullable: false],
        country2:[ d: 'The full name for this entity', nullable: false],
        region3:[ d: 'The full name for this entity', nullable: false],
        country3:[ d: 'The full name for this entity', nullable: false]
    ]

}
