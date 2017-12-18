package gpbench

import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CountryRepo implements GormRepo<Country> {
    //Class domainClass = Country
    //CountryRepo(){ domainClass = Country }
    void beforeCreate(Country country, Map params) {
        country.id = params.id as Long
    }
//
//    @Override
//    void bind(Country entity, Map params, String bindMethod){
//        GormRepo.super.bind(entity, params, bindMethod)
//        entity.id = params.id as Long
//    }

}
