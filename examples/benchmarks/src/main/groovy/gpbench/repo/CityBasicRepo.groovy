package gpbench.repo

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.basic.CityBasic
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@GormRepository
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
