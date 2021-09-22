import gorm.tools.repository.events.BeforeBindEvent
import gorm.tools.repository.events.RepositoryEvent
import gpbench.model.fat.CitySpringEventsRefreshable
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener

/**
 * for the refreshable groovy spring bean and CitySpringEventsRefreshable
 */
@CompileStatic
class CitySpringEventsBean implements ApplicationListener<RepositoryEvent<CitySpringEventsRefreshable>> {

    @Override
    void onApplicationEvent(RepositoryEvent<CitySpringEventsRefreshable> event) {
        if(event instanceof BeforeBindEvent) {
            assert event.data
            //println "reloadable bean beforeBind on CitySpringEvents"
            CitySpringEventsRefreshable entity = event.entity
            Long uid = SecUtil.userId
            Date dt = new Date()
            entity.createdBy = uid
            entity.editedBy = uid
            entity.createdDate = dt
            entity.editedDate = dt
        }
    }

    // Annotation doesn't seem to work in a refreshable bean
//    @EventListener
//    void beforeBind(BeforeBindEvent<CitySpringEvents> event) {
//        println "beforeBind on CitySpringEvents"
//        CitySpringEvents entity = event.entity
//        entity.dateCreatedUser = SecUtil.userId
//        entity.lastUpdatedUser = SecUtil.userId
//    }

}
