package gpbench.model.traits

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gpbench.model.Country
import gpbench.model.Region
import yakworks.commons.lang.IsoDateUtil

@CompileStatic
trait CityTraitFat implements CityTrait, CityTrait2, CityTrait3, DatesTrait {

    void setProps(Map row) {
        StaticSetter.setProps(this, row, false)
    }

}
