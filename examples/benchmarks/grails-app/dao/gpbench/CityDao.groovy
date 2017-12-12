package gpbench

import gorm.tools.dao.GormDao
//import gorm.tools.dao.GormDaoExperimental
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CityDao implements GormDao<City> { //extends DefaultGormDao<City>{

//    CityDao(){
//        domainClass = City
//    }
//
    //    void beforeCreate(City c, Map params) {
    //        //println params
    //    }

    //    City create(Map params) {
    //        //watch for the http://docs.groovy-lang.org/next/html/documentation/core-traits.html#_inheritance_of_state_gotchas, use getters
//        City entity = (City)getDomainClass().newInstance()
//        daoEventPublisher.doBeforeCreate(this, entity, params)
//        bindAndSave(entity, params, "Create")
//        daoEventPublisher.doAfterCreate(this, entity, params)
//        return entity
//    }

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
        //gormDaoApi.persist(c)
        persist(c)
        return c
    }



}
