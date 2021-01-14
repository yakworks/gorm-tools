package gpbench.model

import yakworks.commons.lang.IsoDateUtil
import gpbench.Country
import gpbench.Region

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer

@CompileStatic
trait CityTraitFat implements CityTrait, CityTrait2, CityTrait3, DatesTrait {

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
        //this.properties = row
        this.date1 = IsoDateUtil.parse(row['date1'] as String)
        this.date2 = IsoDateUtil.parseLocalDate(row['date2'] as String) //DateUtil.parse(row['date2'] as String)
        this.date3 = IsoDateUtil.parseLocalDateTime(row['date3'] as String)
        this.date4 = IsoDateUtil.parseLocalDate(row['date4'] as String)

//        region = Region.load(row['region']['id'] as Long)
//        region2 = Region.load(row['region2']['id'] as Long)
//        region3 = Region.load(row['region3']['id'] as Long)
//        country = Country.load(row['country']['id'] as Long)
//        country2 = Country.load(row['country2']['id'] as Long)
//        country3 = Country.load(row['country3']['id'] as Long)
        setAssociation("region", Region, row)
        setAssociation("region2", Region, row)
        setAssociation("region3", Region, row)
        setAssociation("country", Country, row)
        setAssociation("country2", Country, row)
        setAssociation("country3", Country, row)
    }

    void setAssociation(String key, Class assocClass, Map row) {
        if (row[key] != null) {
            Long id = row[key]['id'] as Long
            this[key] = GormEnhancer.findStaticApi(assocClass).load(id)
        }
    }

    static void CityTraitFatConstraints(Object delegate) {
        CityTraitConstraints(delegate)
        CityTrait2Constraints(delegate)
        CityTrait3Constraints(delegate)
        DatesTraitConstraints(delegate)
    }
}
