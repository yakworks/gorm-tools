package gpbench.model


import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class Country implements RepoEntity<Country> {
    String name
    String capital
    String fips104
    String iso2
    String iso3

    static mapping = {
        id generator: "assigned"
//        cache true
    }

    String toString() { name }

}
