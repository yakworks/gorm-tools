package gpbench.fat

import gpbench.Country
import gpbench.Region
import gpbench.model.CityTraitFat
import gpbench.model.DateUserStamp

/**
 * Without @GrailsCompileStatic, fully dynamic
 */
class CityFatDynamic implements CityTraitFat, DateUserStamp {

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
