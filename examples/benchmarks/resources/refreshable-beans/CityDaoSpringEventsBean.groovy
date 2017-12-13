import gorm.tools.dao.events.BeforeCreateEvent
import gpbench.CityDaoSpringEventsRefreshable
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener

@CompileStatic
class CityDaoSpringEventsBean implements ApplicationListener<BeforeCreateEvent<CityDaoSpringEventsRefreshable>> {

    @Override
    void onApplicationEvent(BeforeCreateEvent<CityDaoSpringEventsRefreshable> event) {
        //println "reloadable bean beforeCreate on CityDaoSpringEvents"
        CityDaoSpringEventsRefreshable entity = event.entity
        entity.createdBy = SecUtil.userId
        entity.editedBy = SecUtil.userId
        entity.createdDate = new Date()
        entity.editedDate = new Date()
    }

    // Annotation doesn't seem to work in a refreshable bean
//    @EventListener
//    void beforeCreate(BeforeCreateEvent<CityDaoSpringEvents> event) {
//        println "beforeCreate on CityDaoSpringEvents"
//        CityDaoSpringEvents entity = event.entity
//        entity.dateCreatedUser = SecUtil.userId
//        entity.lastUpdatedUser = SecUtil.userId
//    }

}
