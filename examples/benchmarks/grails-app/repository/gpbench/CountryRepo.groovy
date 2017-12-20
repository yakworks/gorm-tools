package gpbench

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class CountryRepo implements GormRepo<Country> {

    void beforeBind(Country country, Map params, BindAction bindAction) {
        if(bindAction == BindAction.Create) country.id = params.id as Long
    }

}
