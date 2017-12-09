package gpbench.model

import groovy.transform.CompileStatic

@CompileStatic
trait CityTraitFat implements CityTrait, CityTrait2, CityTrait3, DatesTrait{

}

class CityTraitFatConstraints implements CityTraitFat{

    static constraints = {
        importFrom(CityTraitConstraints)
        importFrom(CityTrait2Constraints)
        importFrom(CityTrait3Constraints)
        importFrom DatesTraitConstraints
    }
}
