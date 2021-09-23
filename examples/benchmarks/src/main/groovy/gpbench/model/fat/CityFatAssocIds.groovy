package gpbench.model.fat

import gorm.tools.repository.model.RepoEntity
import gpbench.model.traits.CityTraitFat
import gpbench.model.traits.DateUserStamp
import gpbench.model.traits.StaticSetter
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

// add just the id fields instead of associations, super fast on setters
@Entity
@GrailsCompileStatic
class CityFatAssocIds implements CityTraitFat, DateUserStamp, RepoEntity<CityFatAssocIds> {

    Long regionId
    Long region2Id
    Long region3Id
    Long countryId
    Long country2Id
    Long country3Id

    static constraintsMap = [
        regionId:[ d: 'id', nullable: false],
        countryId:[ d: 'id', nullable: false],
        region2Id:[ d: 'id', nullable: false],
        country2Id:[ d: 'id', nullable: false],
        region3Id:[ d: 'id', nullable: false],
        country3Id:[ d: 'id', nullable: false]
    ]

    void setProps(Map row) {
        StaticSetter.setProps(this, row, false)

        regionId = row['region']['id'] as Long
        region2Id = row['region2']['id'] as Long
        region3Id = row['region3']['id'] as Long

        countryId = row['country']['id'] as Long
        country2Id = row['country2']['id'] as Long
        country3Id = row['country3']['id'] as Long
    }


}
