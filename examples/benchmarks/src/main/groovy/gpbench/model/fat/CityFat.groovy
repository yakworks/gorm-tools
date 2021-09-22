package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTraitFat
import gpbench.model.traits.DateUserStamp
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class CityFat implements CityTraitFat, DateUserStamp, RepoEntity<CityFat> {

    // static belongsTo = [region : Region, country: Country,
    //                     region2: Region, country2: Country,
    //                     region3: Region, country3: Country]

   Region region
   Country country
   Region region2
   Country country2
   Region region3
   Country country3

    //@CompileStatic(TypeCheckingMode.SKIP)
    static constraints = {
        CityTraitFatConstraints(delegate)
        DateUserStampConstraints(delegate)
        region nullable: false
        country nullable: false
        region2 nullable: false
        country2 nullable: false
        region3 nullable: false
        country3 nullable: false
    }

}
