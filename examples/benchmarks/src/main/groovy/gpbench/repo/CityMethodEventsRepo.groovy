package gpbench.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gpbench.SecUtil
import gpbench.model.fat.CityMethodEvents
import grails.persistence.Entity

@GormRepository
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
