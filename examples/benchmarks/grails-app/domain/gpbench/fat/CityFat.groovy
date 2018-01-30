package gpbench.fat

import gorm.tools.beans.IsoDateUtil
import gpbench.Country
import gpbench.Region
import gpbench.model.CityTraitFat
import gpbench.model.CityTraitFatConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import grails.compiler.GrailsCompileStatic
import org.grails.datastore.gorm.GormEnhancer

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
@GrailsCompileStatic
class CityFat implements CityTraitFat, DateUserStamp {

    static belongsTo = [region : Region, country: Country,
                        region2: Region, country2: Country,
                        region3: Region, country3: Country]

//    Region region
//    Country country
//    Region region2
//    Country country2
//    Region region3
//    Country country3

    //@CompileStatic(TypeCheckingMode.SKIP)
    static constraints = {
        importFrom(CityTraitFatConstraints)
        importFrom DateUserStampConstraints
//        region nullable: false
//        country nullable: false
//        region2 nullable: false
//        country2 nullable: false
//        region3 nullable: false
//        country3 nullable: false
    }

}
