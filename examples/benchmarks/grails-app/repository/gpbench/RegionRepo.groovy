package gpbench

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class RegionRepo implements GormRepo<Region> {

    RegionRepo() { domainClass = Region }

    void beforeBind(Region region, Map params, BindAction bindAction) {
        if(bindAction == BindAction.Create) region.id = params.id as Long
    }

//    @Override
//    void bind(Region entity, Map params, String bindMethod){
//        GormRepo.super.bind(entity, params, bindMethod)
//        entity.id = params.id as Long
//    }
}
