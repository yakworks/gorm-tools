package gpbench.fat

import gpbench.Country
import gpbench.Region
import gpbench.model.AuditStamp
import gpbench.model.AuditStampConstraints
import gpbench.model.CityTraitFat
import gpbench.model.CityTraitFatConstraints
import grails.compiler.GrailsCompileStatic

/**
 * Event methods exist in the repository
 */
@GrailsCompileStatic
class CityMethodEvents implements CityTraitFat, AuditStamp {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    static constraints = {
        importFrom CityTraitFatConstraints
        importFrom AuditStampConstraints
    }

    String toString() { name }
}
