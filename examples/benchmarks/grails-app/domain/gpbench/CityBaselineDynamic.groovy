package gpbench

/**
 * Baseline stock grails domain. no Repository or anything else should be attached to this.
 * only normal Grails AST should have touched this.
 */
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

    static belongsTo = [region: Region, country: Country]

    static mapping = {
        //id generator:'native'
        cache true
    }

    static constraints = {
        importFrom(CityBaseline)
    }

    String toString() { name }

}
