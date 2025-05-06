package gpbench.model


import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class Region implements RepoEntity<Region>, Serializable {
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
