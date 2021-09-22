package gpbench.model

import gorm.tools.repository.model.GormRepoEntity
import gpbench.repo.CountryRepo
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class Country implements GormRepoEntity<Country, CountryRepo>{
    String name
    String capital
    String fips104
    String iso2
    String iso3

    static mapping = {
        id generator: "assigned"
//        cache true
    }

    static constraints = {
        name unique: true
        capital nullable: true
    }

    String toString() { name }

}
