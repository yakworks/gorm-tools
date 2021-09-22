package gpbench.repo

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepoListener
import gpbench.model.Region
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@GormRepository
@CompileStatic
class RegionRepo implements GormRepo<Region> {

    @RepoListener
    void beforeBind(Region region, Map params, BeforeBindEvent e) {
        if(e.bindAction == BindAction.Create) region.id = params.id as Long
    }

//    @Override
//    void bind(Region entity, Map params, String bindMethod){
//        GormRepo.super.bind(entity, params, bindMethod)
//        entity.id = params.id as Long
//    }
}
