package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gorm.tools.audit.AuditStamp
import gpbench.model.traits.CityTraitFat
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Event methods exist in the repository
 */
@Entity
@AuditStamp
@GrailsCompileStatic
class CityFatAuditTrail implements CityTraitFat, RepoEntity<CityFatAuditTrail> {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    static constraints = {
        CityTraitFatConstraints(delegate)
    }

    String toString() { name }
}
