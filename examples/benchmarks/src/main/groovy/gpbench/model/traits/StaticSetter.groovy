package gpbench.model.traits

import groovy.transform.CompileStatic

import gorm.tools.repository.RepoUtil
import gpbench.model.Country
import gpbench.model.Region
import yakworks.commons.lang.IsoDateUtil

@CompileStatic
class StaticSetter {


    static void setProps(CityTraitFat entity, Map row, boolean doAssocs) {
        entity.name = row['name']
        entity.shortCode = row['shortCode']
        entity.state = row['state']
        entity.countryName = row['countryName']
        entity.latitude = row['latitude'] as BigDecimal
        entity.longitude = row['longitude'] as BigDecimal

        entity.name2 = row['name2']
        entity.shortCode2 = row['shortCode2']
        entity.state2 = row['state2']
        entity.countryName2 = row['countryName2']
        entity.latitude2 = row['latitude2'] as BigDecimal
        entity.longitude2 = row['longitude2'] as BigDecimal

        entity.name3 = row['name3']
        entity.shortCode3 = row['shortCode3']
        entity.state3 = row['state3']
        entity.countryName3 = row['countryName3']
        entity.latitude3 = row['latitude3'] as BigDecimal
        entity.longitude3 = row['longitude3'] as BigDecimal
        //this.properties = row
        entity.date1 = IsoDateUtil.parse(row['date1'] as String)
        entity.date2 = IsoDateUtil.parseLocalDate(row['date2'] as String) //DateUtil.parse(row['date2'] as String)
        entity.date3 = IsoDateUtil.parseLocalDateTime(row['date3'] as String)
        entity.date4 = IsoDateUtil.parseLocalDate(row['date4'] as String)

        if(doAssocs) {
            def asAssoc = (CityTraitFatWithAssoc)entity
            // withAssoc.region = Region.load(row['region']['id'] as Long)
            // withAssoc.region2 = Region.load(row['region2']['id'] as Long)
            // withAssoc.region3 = Region.load(row['region3']['id'] as Long)
            // withAssoc.country = Country.load(row['country']['id'] as Long)
            // withAssoc.country2 = Country.load(row['country2']['id'] as Long)
            // withAssoc.country3 = Country.load(row['country3']['id'] as Long)
            setAssociation(asAssoc, "region", Region, row)
            setAssociation(asAssoc, "region2", Region, row)
            setAssociation(asAssoc, "region3", Region, row)
            setAssociation(asAssoc, "country", Country, row)
            setAssociation(asAssoc, "country2", Country, row)
            setAssociation(asAssoc, "country3", Country, row)
        }

    }

    static void setAssociation(CityTraitFatWithAssoc entity, String key, Class assocClass, Map row) {
        if (row[key] != null) {
            Long id = row[key]['id'] as Long
            entity[key] = RepoUtil.findRepo(assocClass).load(id)
            // this[key] = GormEnhancer.findStaticApi(assocClass).load(id)
        }
    }

}
