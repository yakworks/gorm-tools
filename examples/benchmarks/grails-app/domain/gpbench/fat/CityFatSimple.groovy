package gpbench.fat

import grails.compiler.GrailsCompileStatic

/**
 * No Association, not constraints, etc..
 */
@GrailsCompileStatic
class CityFatSimple {
    String name
    String shortCode
    String state
    String countryName

    BigDecimal latitude
    BigDecimal longitude

    String name2
    String shortCode2
    String state2
    String countryName2

    BigDecimal latitude2
    BigDecimal longitude2

    String name3
    String shortCode3
    String state3
    String countryName3

    BigDecimal latitude3
    BigDecimal longitude3

}
