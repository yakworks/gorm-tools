package gpbench.model.basic

import gorm.tools.repository.model.GormRepoEntity
import gpbench.SecUtil
import gpbench.model.Country
import gpbench.model.Region
import gpbench.repo.CityDynamicRepo
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

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
        dateCreatedUser = SecUtil.userId
    }

    def beforeUpdate() {
        lastUpdatedUser = SecUtil.userId
    }

}
