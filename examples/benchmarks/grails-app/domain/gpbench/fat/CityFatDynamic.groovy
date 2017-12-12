package gpbench.fat

import gpbench.Country
import gpbench.Region
import gpbench.model.CityTrait
import gpbench.model.CityTrait2Constraints
import gpbench.model.CityTrait3Constraints
import gpbench.model.CityTraitConstraints
import gpbench.model.CityTraitFat
import gpbench.model.CityTraitFatConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import grails.compiler.GrailsCompileStatic

/**
 * Without @GrailsCompileStatic, fully dynamic
 */
class CityFatDynamic implements CityTraitFat, DateUserStamp{

    static belongsTo = [region:Region, country:Country,
                        region2:Region, country2:Country,
                        region3:Region, country3:Country]

    //@CompileStatic(TypeCheckingMode.SKIP)
    static constraints = {
        importFrom(CityTraitFatConstraints)
        importFrom DateUserStampConstraints
    }

}
