package gpbench

import gorm.tools.repository.GormRepo
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityMethodEventsRepo implements GormRepo<CityMethodEvents> {

//    void beforeBind(CityMethodEvents entity, Map params) {
//        entity.createdBy = SecUtil.userId
//        entity.editedBy = SecUtil.userId
//        entity.createdDate = new Date()
//        entity.editedDate = new Date()
//    }

    void beforePersist(CityMethodEvents entity, Map args) {
        Long uid = SecUtil.userId
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

//    void beforeUpdate(CityMethodEvents entity, Map params) {
//        entity.lastUpdatedUser = SecUtil.userId
//        entity.lastUpdated = new Date()
//    }

}
