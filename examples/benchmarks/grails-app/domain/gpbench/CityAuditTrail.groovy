package gpbench

import gorm.AuditStamp
import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import grails.compiler.GrailsCompileStatic

@AuditStamp
@GrailsCompileStatic
class CityAuditTrail implements CityTrait {

    static belongsTo = [region: Region, country: Country]

    static mapping = {
        //id column: 'id', generator:'gorm.tools.idgen.SpringIdGenerator'
        //cache true
    }

    static constraints = {
        importFrom CityTraitConstraints
    }

    String toString() { name }

}
