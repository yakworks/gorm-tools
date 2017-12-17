package gpbench

import gorm.tools.repository.GormRepo
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityRepoMethodEventsRepo implements GormRepo<CityMethodEvents> {

    void beforeCreate(CityMethodEvents entity, Map params) {
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

//    void beforeUpdate(CityMethodEvents entity, Map params) {
//        entity.lastUpdatedUser = SecUtil.userId
//        entity.lastUpdated = new Date()
//    }

}
