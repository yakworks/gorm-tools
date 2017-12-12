
import gpbench.fat.CityFat
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
class DummyListenerBean {

    @Listener
    void beforeValidate(ValidationEvent event) {

    }

//    @Listener
//    void beforeInsert(PreInsertEvent event) {
//
//    }
//
//    @Listener
//    void beforeUpdate(PreUpdateEvent event) {
//
//    }

}
