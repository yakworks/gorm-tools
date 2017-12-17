package gpbench

import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CityRepo implements GormRepo<City> {

    boolean enableEvents = false

    //@NotTransactional
    //@CompileDynamic
    City bindWithSetters(Map row) {
        Region r = Region.load(row['region']['id'] as Long)
        Country country = Country.load(row['country']['id'] as Long)

        City c = new City()
        c.name = row.name
        c.shortCode = row.name

        c.latitude = row.latitude as BigDecimal
        c.longitude = row.longitude as BigDecimal

        c.region = r
        c.country = country
        return c
    }

    City insertWithSetter(Map row) {
        City c = bindWithSetters(row)
        persist(c)
        return c
    }


}
