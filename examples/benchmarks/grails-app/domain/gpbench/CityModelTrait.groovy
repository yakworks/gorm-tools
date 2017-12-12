package gpbench

import gpbench.model.CityModel
import grails.compiler.GrailsCompileStatic


@GrailsCompileStatic
class CityModelTrait implements CityModel{

    static belongsTo = [Region, Country]

    static mapping = {
        cache true
    }

    static constraints = {
        importFrom(CityBaselineDynamic)
    }

    String toString() { name }

}
