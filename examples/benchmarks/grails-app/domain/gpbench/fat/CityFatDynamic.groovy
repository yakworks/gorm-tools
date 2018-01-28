package gpbench.fat

import gorm.tools.beans.IsoDateUtil
import gpbench.Country
import gpbench.Region
import gpbench.model.CityTraitFat
import gpbench.model.CityTraitFatConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import org.grails.datastore.gorm.GormEnhancer

/**
 * Without @GrailsCompileStatic, fully dynamic
 */
class CityFatDynamic implements CityTraitFat, DateUserStamp {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

    //@CompileStatic(TypeCheckingMode.SKIP)
    static constraints = {
        importFrom(CityTraitFatConstraints)
        importFrom DateUserStampConstraints
    }

}
