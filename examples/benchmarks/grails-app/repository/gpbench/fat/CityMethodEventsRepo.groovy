package gpbench.fat

import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gpbench.SecUtil
import gpbench.fat.CityMethodEvents
import groovy.transform.CompileStatic

//@Transactional
@CompileStatic
class CityMethodEventsRepo implements GormRepo<CityMethodEvents> {

    @RepoListener
    void beforePersist(CityMethodEvents entity, BeforePersistEvent e) {
        Long uid = SecUtil.userId
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

}
