package gpbench

import gpbench.model.AuditStamp
import gpbench.model.AuditStampConstraints
import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import grails.compiler.GrailsCompileStatic

/**
 * Event methods exist in the dao
 */
@GrailsCompileStatic
class CityDaoMethodEvents implements CityTrait, AuditStamp {

    static belongsTo = [region: Region, country: Country]

    static constraints = {
        importFrom CityTraitConstraints
        importFrom AuditStampConstraints
    }

    String toString() { name }
}
