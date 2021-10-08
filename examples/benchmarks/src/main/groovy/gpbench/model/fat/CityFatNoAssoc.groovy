package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.traits.CityTraitFat
import gpbench.model.traits.DateUserStamp
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

// The FASTEST one without associations, can see it doubles the time for just 3
// but its insignificant to add just the id fields
@Entity
@GrailsCompileStatic
class CityFatNoAssoc implements CityTraitFat, DateUserStamp, RepoEntity<CityFatNoAssoc> {

}
