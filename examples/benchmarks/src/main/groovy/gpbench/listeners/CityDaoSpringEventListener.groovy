package gpbench.listeners

import gorm.tools.dao.events.BeforeCreateEvent
import gpbench.CityDaoSpringEvents
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.event.EventListener

import javax.annotation.ManagedBean

@ManagedBean
@CompileStatic
class CityDaoSpringEventListener {

    @EventListener
    void beforeCreate(BeforeCreateEvent<CityDaoSpringEvents> event) {
        //println "beforeCreate on CityDaoSpringEvents"
        CityDaoSpringEvents entity = event.entity
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

    //@Listener(CityDaoPerisistenceEvents)
//    void beforeUpdate(BeforeUpdateEvent event) {
//        CityDaoSpringEvents entity = (CityDaoSpringEvents)event.entityObject
//        entity.lastUpdatedUser = SecUtil.userId
//        entity.lastUpdated = new Date()
//    }

}
