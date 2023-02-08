package gpbench.model.basic

import gorm.tools.repository.model.GormRepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.repo.CityDynamicRepo
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.security.user.CurrentUserHolder

@Entity
@IdEqualsHashCode
class CityDynamic implements GormRepoEntity<CityDynamic, CityDynamicRepo> {
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

    // static belongsTo = [Region, Country]

    static mapping = {
//        cache true
    }

    static constraints = {
        importFrom(CityBaselineDynamic)
    }

    String toString() { name }

    def beforeInsert() {
        dateCreatedUser = CurrentUserHolder.user.id as Long
    }

    def beforeUpdate() {
        lastUpdatedUser = CurrentUserHolder.user.id as Long
    }

}
