package gpbench.basic

import gpbench.Country
import gpbench.Region
import gpbench.model.CityTrait
import gpbench.model.DateUserStamp
import grails.compiler.GrailsCompileStatic

/**
 * repo Baseline. This has a repo and has been touched by the gorm-tools AST
 */
@GrailsCompileStatic
class CityBasic implements CityTrait, DateUserStamp {

    static belongsTo = [region: Region, country: Country]

//    static mapping = {
//        cache true
//    }

    static constraints = {
        CityTraitConstraints(delegate)
        DateUserStampConstraints(delegate)
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
