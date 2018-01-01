package gpbench.fat

import gorm.tools.beans.IsoDateUtil
import gpbench.Country
import gpbench.Region
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

    void setProps(Map row) {
        this.name = row['name']
        this.shortCode = row['shortCode']
        this.state = row['state']
        this.countryName = row['countryName']
        this.latitude = row['latitude'] as BigDecimal
        this.longitude = row['longitude'] as BigDecimal

        this.name2 = row['name2']
        this.shortCode2 = row['shortCode2']
        this.state2 = row['state2']
        this.countryName2 = row['countryName2']
        this.latitude2 = row['latitude2'] as BigDecimal
        this.longitude2 = row['longitude2'] as BigDecimal

        this.name3 = row['name3']
        this.shortCode3 = row['shortCode3']
        this.state3 = row['state3']
        this.countryName3 = row['countryName3']
        this.latitude3 = row['latitude3'] as BigDecimal
        this.longitude3 = row['longitude3'] as BigDecimal

    }

}
