
import gpbench.CityRefreshableBeanEvents
import gpbench.SecUtil
import grails.events.annotation.gorm.Listener
import grails.util.Holders
import groovy.transform.CompileStatic
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.engine.event.ValidationEvent
import org.springframework.beans.factory.annotation.Autowired
//import org.grails.events.gorm.GormDispatcherRegistrar

/**
 * see https://docs.spring.io/spring/docs/5.0.2.RELEASE/spring-framework-reference/languages.html#dynamic-language-refreshable-beans
 */

@CompileStatic
class CityRefreshableBeanListener {

    @Listener(CityRefreshableBeanEvents)
    void beforeValidate(ValidationEvent event) {
        CityRefreshableBeanEvents entity = (CityRefreshableBeanEvents)event.entityObject
        entity.dateCreatedUser = SecUtil.userId
        entity.lastUpdatedUser = SecUtil.userId

        entity.dateCreated = new Date()
        entity.lastUpdated = new Date()
    }

    @Listener(CityRefreshableBeanEvents)
    void beforeInsert(PreInsertEvent event) {
        EntityAccess ea = event.entityAccess
        ea.setProperty("dateCreatedUser", SecUtil.userId)
        ea.setProperty("dateCreated", new Date())
        ea.setProperty("lastUpdatedUser", SecUtil.userId)
        ea.setProperty("lastUpdated", new Date())
        //println "subscriber size: " + Holders.applicationContext.gormDispatchEventRegistrar.subscribers.size()
    }

    @Listener(CityRefreshableBeanEvents)
    void beforeUpdate(PreUpdateEvent event) {
        //println "beforeUpdate"
        EntityAccess ea = event.entityAccess
        ea.setProperty("lastUpdatedUser", SecUtil.userId)
        ea.setProperty("lastUpdated", new Date())
    }

}
