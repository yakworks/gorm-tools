package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTraitFat
import gpbench.model.traits.DateUserStamp
import grails.persistence.Entity

/**
 * Without @GrailsCompileStatic, fully dynamic
 */
@Entity
class CityFatDynamic implements CityTraitFat, DateUserStamp, RepoEntity<CityFatDynamic> {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    //@CompileStatic(TypeCheckingMode.SKIP)
    static constraints = {
        CityTraitFatConstraints(delegate)
        DateUserStampConstraints(delegate)
//        region nullable: false
//        country nullable: false
//        region2 nullable: false
//        country2 nullable: false
//        region3 nullable: false
//        country3 nullable: false
    }

}
