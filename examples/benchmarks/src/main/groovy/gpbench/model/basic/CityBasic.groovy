package gpbench.model.basic

import gorm.tools.repository.model.RepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTrait
import gpbench.model.traits.DateUserStamp
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

/**
 * repo Baseline. This has a repo and has been touched by the gorm-tools AST
 */
@Entity
@GrailsCompileStatic
class CityBasic implements CityTrait, DateUserStamp, RepoEntity<CityBasic> {

    // static belongsTo = [region: Region, country: Country]

//    static mapping = {
//        cache true
//    }


    Region region
    Country country

    String toString() { name }

//    def beforeInsert() {
//        dateCreatedUser = SecUtil.userId
//    }
//
//    def beforeUpdate() {
//        lastUpdatedUser = SecUtil.userId
//    }

}
