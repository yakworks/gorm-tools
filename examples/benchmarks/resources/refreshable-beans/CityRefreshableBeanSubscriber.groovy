import gpbench.model.CityRefreshableBeanEvents
import grails.events.annotation.Subscriber
import org.grails.datastore.mapping.engine.event.PostInsertEvent

//import org.grails.events.gorm.GormDispatcherRegistrar

/**
 * see https://docs.spring.io/spring/docs/5.0.2.RELEASE/spring-framework-reference/languages.html#dynamic-language-refreshable-beans
 */

//@CompileStatic
class CityRefreshableBeanSubscriber {

    //@Autowired CityRefreshableBean cityRefreshableBean
    int subnum

    @Subscriber
    void postInsert(PostInsertEvent event) {
        if(event.entityObject instanceof CityRefreshableBeanEvents){
            assert event.entityObject.lastUpdated
            //println "CityRefreshableBeanEvents $subnum ${event.entityObject.id} "
        }
        //println "CityRefreshableBeanSubscriber"
        //println "subscriber size: " + Holders.applicationContext.gormDispatchEventRegistrar.subscribers.size()
    }

}
