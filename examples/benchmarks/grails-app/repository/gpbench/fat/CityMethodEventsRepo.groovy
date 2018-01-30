package gpbench.fat

import gorm.tools.repository.GormRepo
import gpbench.SecUtil
import gpbench.fat.CityMethodEvents
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityMethodEventsRepo implements GormRepo<CityMethodEvents> {

    void beforePersist(CityMethodEvents entity, Map args) {
        Long uid = SecUtil.userId
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

}
