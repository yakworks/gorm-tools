package gpbench

import gorm.tools.dao.GormDao
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

@Transactional
@CompileStatic
class RegionDao implements GormDao<Region> {

    RegionDao(){ domainClass = Region }

    void beforeCreate(Region region, Map params) {
        region.id = params.id as Long
    }

//    @Override
//    void bind(Region entity, Map params, String bindMethod){
//        GormDao.super.bind(entity, params, bindMethod)
//        entity.id = params.id as Long
//    }
}
