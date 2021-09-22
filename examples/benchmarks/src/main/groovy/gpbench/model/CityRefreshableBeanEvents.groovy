package gpbench.model

import gorm.tools.repository.model.RepoEntity
import gpbench.model.traits.CityTrait
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/*
 Audit stamp fields are set by a gorm event listener defined in external groovy script.
 */

@Entity
@GrailsCompileStatic
class CityRefreshableBeanEvents implements CityTrait, RepoEntity<CityRefreshableBeanEvents> {

    static belongsTo = [region: Region, country: Country]

    Date dateCreated
    Date lastUpdated
    Long dateCreatedUser
    Long lastUpdatedUser

    static mapping = {
        //cache true
    }

    static constraints = {
        CityTraitConstraints(delegate)
        dateCreated nullable: false, display: false, editable: false, bindable: false
        lastUpdated nullable: false, display: false, editable: false, bindable: false
        dateCreatedUser nullable: false, display: false, editable: false, bindable: false
        lastUpdatedUser nullable: false, display: false, editable: false, bindable: false
    }

    String toString() { name }
}
