package gpbench.model

import gorm.tools.repository.model.GormRepoEntity
import gpbench.repo.RegionRepo
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@GrailsCompileStatic
class Region implements GormRepoEntity<Region, RegionRepo>, Serializable {
    static belongsTo = [country: Country]

    String name
    String code
    String admCode



    static mapping = {
//        cache true
        id generator: "assigned"

    }

    static constraints = {
        name nullable: false
        code nullable: false
    }

    String toString() { code }

}
