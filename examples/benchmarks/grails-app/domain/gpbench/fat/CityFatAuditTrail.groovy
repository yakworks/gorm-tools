package gpbench.fat

import gpbench.Country
import gpbench.Region
import gorm.tools.audit.AuditStamp
import gpbench.model.CityTraitFat
import grails.compiler.GrailsCompileStatic

/**
 * Event methods exist in the repository
 */
@AuditStamp
@GrailsCompileStatic
class CityFatAuditTrail implements CityTraitFat {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    static constraints = {
        CityTraitFatConstraints(delegate)
    }

    String toString() { name }
}
