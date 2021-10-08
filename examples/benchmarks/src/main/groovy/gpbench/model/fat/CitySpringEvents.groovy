package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.AuditStamp
import gpbench.model.traits.CityTraitFatWithAssoc
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * No explicit repository for this, relies on the DefaultGormRepo that the plugin setus up.
 * DateUserStamp fields are set from repository persistence events listener.
 */
@Entity
@GrailsCompileStatic
class CitySpringEvents implements CityTraitFatWithAssoc, AuditStamp, RepoEntity<CitySpringEvents> {
    //
    // static belongsTo = [region : Region, country: Country,
    //                     region2: Region, country2: Country,
    //                     region3: Region, country3: Country]

    Region region
    Country country
    Region region2
    Country country2
    Region region3
    Country country3

    String toString() { name }
}
