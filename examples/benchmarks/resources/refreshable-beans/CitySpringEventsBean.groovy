import gorm.tools.repository.events.BeforeBindEvent
import gpbench.fat.CitySpringEventsRefreshable
import gpbench.SecUtil
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener

@CompileStatic
class CitySpringEventsBean implements ApplicationListener<BeforeBindEvent<CitySpringEventsRefreshable>> {

    @Override
    void onApplicationEvent(BeforeBindEvent<CitySpringEventsRefreshable> event) {
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

    // Annotation doesn't seem to work in a refreshable bean
//    @EventListener
//    void beforeBind(BeforeBindEvent<CitySpringEvents> event) {
//        println "beforeBind on CitySpringEvents"
//        CitySpringEvents entity = event.entity
//        entity.dateCreatedUser = SecUtil.userId
//        entity.lastUpdatedUser = SecUtil.userId
//    }

}
