package gpbench

import gpbench.model.AuditStamp
import gpbench.model.AuditStampConstraints
import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import grails.compiler.GrailsCompileStatic

/**
 * No explicit dao for this, relies on the DefaultGormDao that the plugin setus up.
 * DateUserStamp fields are set from dao persistence events listener.
 */
@GrailsCompileStatic
class CityDaoSpringEventsRefreshable implements CityTrait, AuditStamp {

    static belongsTo = [region: Region, country: Country]

    static constraints = {
        importFrom CityTraitConstraints
        importFrom AuditStampConstraints
    }

    String toString() { name }
}
