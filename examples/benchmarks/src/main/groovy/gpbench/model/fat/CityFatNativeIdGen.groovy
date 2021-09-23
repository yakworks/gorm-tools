package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTraitFatWithAssoc
import gpbench.model.traits.DateUserStamp
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 *
 */
@Entity
@GrailsCompileStatic
class CityFatNativeIdGen implements CityTraitFatWithAssoc, DateUserStamp, RepoEntity<CityFatNativeIdGen> {

    // static belongsTo = [region : Region, country: Country,
    //                     region2: Region, country2: Country,
    //                     region3: Region, country3: Country]

    Region region
    Country country
    Region region2
    Country country2
    Region region3
    Country country3

    static mapping = {
        id generator: "native"
    }

}
