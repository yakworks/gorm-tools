package gpbench

import gorm.tools.dao.GormDao
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityDaoMethodEventsDao implements GormDao<CityDaoMethodEvents> {

    void beforeCreate(CityDaoMethodEvents entity, Map params) {
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

//    void beforeUpdate(CityDaoMethodEvents entity, Map params) {
//        entity.lastUpdatedUser = SecUtil.userId
//        entity.lastUpdated = new Date()
//    }

}
