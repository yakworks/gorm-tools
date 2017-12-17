package gpbench.listeners

import gorm.tools.repository.events.BeforeCreateEvent
import gpbench.CitySpringEvents
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.event.EventListener

import javax.annotation.ManagedBean

@ManagedBean
@CompileStatic
class CitySpringAnnotationEventListener {

    @EventListener
    void beforeCreate(BeforeCreateEvent<CitySpringEvents> event) {
        //println "beforeCreate on CitySpringEvents"
        CitySpringEvents entity = event.entity
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

}
