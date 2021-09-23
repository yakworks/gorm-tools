package gpbench.model.dynamic

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTraitFatWithAssoc
import gpbench.model.traits.DateUserStamp
import grails.persistence.Entity

/**
 * Without @GrailsCompileStatic, dynamic but it seems to benefit from the compilestaic on the traits
 */
@Entity
class CityFatDynamic implements CityTraitFatWithAssoc, DateUserStamp, RepoEntity<CityFatDynamic> {

    // static belongsTo = [region : Region, country: Country,
    //                     region2: Region, country2: Country,
    //                     region3: Region, country3: Country]

    Region region
    Country country
    Region region2
    Country country2
    Region region3
    Country country3

}
