package gpbench.model.fat

import gorm.tools.repository.model.GormRepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.AuditStamp
import gpbench.model.traits.CityTraitFat
import gpbench.repo.CityMethodEventsRepo
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Event methods exist in the repository
 */
@Entity
@GrailsCompileStatic
class CityMethodEvents implements CityTraitFat, AuditStamp, GormRepoEntity<CityMethodEvents, CityMethodEventsRepo> {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    static constraints = {
        CityTraitFatConstraints(delegate)
        AuditStampConstraints(delegate)
    }

    String toString() { name }
}
