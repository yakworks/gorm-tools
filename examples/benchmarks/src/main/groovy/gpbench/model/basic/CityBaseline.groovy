package gpbench.model.basic

import gpbench.model.Country
import gpbench.model.Region
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Baseline stock grails domain. no repo, no Traits or anything else should be attached to this
 * only stock out-of-the-box Grails/Gorm AST should have touched this.
 */
@Entity
@GrailsCompileStatic
class CityBaseline {
    String name
    String shortCode

    BigDecimal latitude
    BigDecimal longitude

    Date dateCreated
    Date lastUpdated

    //these don't do anything and are just here to equalize the number of fields
    Long dateCreatedUser
    Long lastUpdatedUser

    Region region
    Country country
    String state
    String countryName

    // static belongsTo = [region: Region, country: Country]

    static constraints = {
        name nullable: false
        shortCode nullable: false
        latitude nullable: false, scale: 4, max: 90.00
        longitude nullable: false, scale: 4, max: 380.00
        region nullable: false
        country nullable: false
        state nullable: true
        countryName nullable: true

        dateCreated nullable: true, display: false, editable: false, bindable: false
        lastUpdated nullable: true, display: false, editable: false, bindable: false
        dateCreatedUser nullable: true, display: false, editable: false, bindable: false
        lastUpdatedUser nullable: true, display: false, editable: false, bindable: false
    }

    String toString() { name }

}
