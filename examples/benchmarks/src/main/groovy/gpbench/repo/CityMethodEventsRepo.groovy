package gpbench.repo

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gpbench.model.fat.CityMethodEvents
import grails.persistence.Entity
import yakworks.security.user.CurrentUserHolder

@GormRepository
@CompileStatic
class CityMethodEventsRepo implements GormRepo<CityMethodEvents> {

    @RepoListener
    void beforePersist(CityMethodEvents entity, BeforePersistEvent e) {
        Long uid = CurrentUserHolder.user.id as Long
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

}
