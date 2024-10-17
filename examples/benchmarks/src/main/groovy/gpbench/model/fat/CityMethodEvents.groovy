package gpbench.model.fat


import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.AuditStamp
import gpbench.model.traits.CityTraitFatWithAssoc
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * Event methods exist in the repository
 */
@Entity
@GrailsCompileStatic
class CityMethodEvents implements CityTraitFatWithAssoc, AuditStamp, RepoEntity<CityMethodEvents> {

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
