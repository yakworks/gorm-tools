package gpbench.basic

import gpbench.Country
import gpbench.Region
import gpbench.SecUtil

class CityDynamic {
    //transient springSecurityService

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
    Long dateCreatedUser
    Long lastUpdatedUser

    static belongsTo = [Region, Country]

    static mapping = {
//        cache true
    }

    static constraints = {
        importFrom(CityBaselineDynamic)
    }

    String toString() { name }

    def beforeInsert() {
        dateCreatedUser = SecUtil.userId
    }

    def beforeUpdate() {
        lastUpdatedUser = SecUtil.userId
    }

}
