package gpbench.model.basic

import gpbench.model.Country
import gpbench.model.Region
import grails.persistence.Entity

/**
 * Baseline stock grails domain. no Repository or anything else should be attached to this.
 * only normal Grails AST should have touched this.
 */
@Entity
class CityBaselineDynamic {
    String name
    String shortCode

    BigDecimal latitude
    BigDecimal longitude

    Region region
    Country country
    String state
    String countryName

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    // static belongsTo = [region: Region, country: Country]

//     static mapping = {
//         //id generator:'native'
// //        cache true
//     }

    static constraints = {
        importFrom(CityBaseline)
    }

    String toString() { name }

}
