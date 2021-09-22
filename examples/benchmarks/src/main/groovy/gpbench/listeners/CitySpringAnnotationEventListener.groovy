package gpbench.listeners

import gorm.tools.repository.events.BeforeBindEvent
import gpbench.model.fat.CitySpringEvents
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * for Spring Event Listener Bean for CitySpringEvents repo events
 */
@Component
@CompileStatic
class CitySpringAnnotationEventListener {

    @EventListener
    void beforeBind(BeforeBindEvent<CitySpringEvents> event) {
        assert event.data
        //println "beforeBind on CitySpringEvents"
        CitySpringEvents entity = event.entity
        Long uid = SecUtil.userId
        Date dt = new Date()
        entity.createdBy = uid
        entity.editedBy = uid
        entity.createdDate = dt
        entity.editedDate = dt
    }

}
