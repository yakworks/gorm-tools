package gpbench.basic

import gorm.tools.repository.GormRepo
import gpbench.Country
import gpbench.Region
import gpbench.basic.CityBasic
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CityBasicRepo implements GormRepo<CityBasic> {

    Boolean enableEvents = false

    //@NotTransactional
    //@CompileDynamic
    CityBasic bindWithSetters(Map row) {
        Region r = Region.load(row['region']['id'] as Long)
        Country country = Country.load(row['country']['id'] as Long)

        CityBasic c = new CityBasic()
        c.name = row.name
        c.shortCode = row.name

        c.latitude = row.latitude as BigDecimal
        c.longitude = row.longitude as BigDecimal

        c.region = r
        c.country = country
        return c
    }

    CityBasic insertWithSetter(Map row) {
        CityBasic c = bindWithSetters(row)
        persist(c)
        return c
    }


}
