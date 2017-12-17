import gorm.tools.repository.events.BeforeCreateEvent
import gpbench.CitySpringEventsRefreshable
import gpbench.CitySpringEventsRefreshable
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener

@CompileStatic
class CityDaoSpringEventsBean implements ApplicationListener<BeforeCreateEvent<CitySpringEventsRefreshable>> {

    @Override
    void onApplicationEvent(BeforeCreateEvent<CitySpringEventsRefreshable> event) {
        //println "reloadable bean beforeCreate on CitySpringEvents"
        CitySpringEventsRefreshable entity = event.entity
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

    // Annotation doesn't seem to work in a refreshable bean
//    @EventListener
//    void beforeCreate(BeforeCreateEvent<CitySpringEvents> event) {
//        println "beforeCreate on CitySpringEvents"
//        CitySpringEvents entity = event.entity
//        entity.dateCreatedUser = SecUtil.userId
//        entity.lastUpdatedUser = SecUtil.userId
//    }

}
