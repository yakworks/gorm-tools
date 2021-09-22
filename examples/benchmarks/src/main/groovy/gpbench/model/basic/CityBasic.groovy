package gpbench.model.basic

import gorm.tools.repository.model.GormRepoEntity
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.traits.CityTrait
import gpbench.model.traits.DateUserStamp
import gpbench.repo.CityBasicRepo
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

/**
 * repo Baseline. This has a repo and has been touched by the gorm-tools AST
 */
@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class CityBasic implements CityTrait, DateUserStamp, GormRepoEntity<CityBasic, CityBasicRepo> {

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
