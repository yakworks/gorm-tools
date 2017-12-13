package gpbench

import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import grails.compiler.GrailsCompileStatic

/*
 Audit stamp fields are set by a gorm event listener defined in external groovy script.
 */

@GrailsCompileStatic
class CityRefreshableBeanEvents implements CityTrait {

    static belongsTo = [region: Region, country: Country]

    Date dateCreated
    Date lastUpdated
    Long dateCreatedUser
    Long lastUpdatedUser

    static mapping = {
        //cache true
    }

    static constraints = {
        importFrom(CityTraitConstraints)

        dateCreated nullable: false, display: false, editable: false, bindable: false
        lastUpdated nullable: false, display: false, editable: false, bindable: false
        dateCreatedUser nullable: false, display: false, editable: false, bindable: false
        lastUpdatedUser nullable: false, display: false, editable: false, bindable: false
    }

    String toString() { name }
}
