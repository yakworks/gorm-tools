package gpbench

import gorm.tools.dao.GormDao
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CountryDao implements GormDao<Country> {
    //Class domainClass = Country
    //CountryDao(){ domainClass = Country }
    void beforeCreate(Country country, Map params) {
        country.id = params.id as Long
    }
//
//    @Override
//    void bind(Country entity, Map params, String bindMethod){
//        GormDao.super.bind(entity, params, bindMethod)
//        entity.id = params.id as Long
//    }

}
