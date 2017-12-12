package gpbench

import gpbench.model.CityTrait
import gpbench.model.CityTraitConstraints
import gpbench.model.DateUserStamp
import gpbench.model.DateUserStampConstraints
import grails.compiler.GrailsCompileStatic

/**
 * Dao Baseline. This has a DAO and has been touched by the gorm-tools AST
 */
@GrailsCompileStatic
class City implements CityTrait, DateUserStamp {

    static belongsTo = [region:Region, country:Country]

//    static mapping = {
//        cache true
//    }

    static constraints = {
        importFrom CityTraitConstraints
        importFrom DateUserStampConstraints
    }

    String toString() { name }

//    def beforeInsert() {
//        dateCreatedUser = SecUtil.userId
//    }
//
//    def beforeUpdate() {
//        lastUpdatedUser = SecUtil.userId
//    }

}
