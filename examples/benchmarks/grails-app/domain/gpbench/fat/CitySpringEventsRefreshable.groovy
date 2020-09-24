package gpbench.fat

import gpbench.Country
import gpbench.Region
import gpbench.model.AuditStamp
import gpbench.model.CityTraitFat
import grails.compiler.GrailsCompileStatic

/**
 * No explicit repository for this, relies on the DefaultGormRepo that the plugin setus up.
 * DateUserStamp fields are set from repository persistence events listener.
 */
@GrailsCompileStatic
class CitySpringEventsRefreshable implements CityTraitFat, AuditStamp {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    static constraints = {
        CityTraitFatConstraints(delegate)
        AuditStampConstraints(delegate)
    }

    String toString() { name }
}
