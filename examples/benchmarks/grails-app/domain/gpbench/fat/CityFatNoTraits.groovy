package gpbench.fat

import gpbench.Country
import gpbench.Region
import grails.compiler.GrailsCompileStatic

/**
 * Single version of without traits
 */
@GrailsCompileStatic
class CityFatNoTraits {
    String name
    String shortCode

    BigDecimal latitude
    BigDecimal longitude

    String state
    String countryName

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    String name2
    String shortCode2
    String state2
    String countryName2

    BigDecimal latitude2
    BigDecimal longitude2

    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

    static belongsTo = [region:Region, country:Country, region2:Region, country2:Country, region3:Region, country3:Country]

    static mapping = {
        cache true
    }

    static constraints = {
        name blank: false, nullable: false
        shortCode blank: false, nullable: false
        latitude nullable: false, scale: 4, max:90.00
        longitude nullable: false, scale: 4, max:380.00
        region nullable: true
        country nullable: true
        state nullable: true
        countryName nullable: true

        name2 blank: false, nullable: false
        shortCode2 blank: false, nullable: false
        latitude2 nullable: false, scale: 4, max:90.00
        longitude2 nullable: false, scale: 4, max:380.00
        region2 nullable: true
        country2 nullable: true
        state2 nullable: true
        countryName2 nullable: true

        name3 blank: false, nullable: false
        shortCode3 blank: false, nullable: false
        latitude3 nullable: false, scale: 4, max:90.00
        longitude3 nullable: false, scale: 4, max:380.00
        region3 nullable: true
        country3 nullable: true
        state3 nullable: true
        countryName3 nullable: true

        dateCreated nullable:true,display:false,editable:false,bindable:false
        lastUpdated nullable:true,display:false,editable:false,bindable:false
        dateCreatedUser nullable:true,display:false,editable:false,bindable:false
        lastUpdatedUser nullable:true,display:false,editable:false,bindable:false
    }

    String toString() { name }

}
