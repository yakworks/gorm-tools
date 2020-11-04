package gpbench

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@CompileStatic
class CountryRepo implements GormRepo<Country> {

    @RepoListener
    void beforeBind(Country country, Map params, BeforeBindEvent e) {
        if(e.bindAction == BindAction.Create) country.id = params.id as Long
    }

}
