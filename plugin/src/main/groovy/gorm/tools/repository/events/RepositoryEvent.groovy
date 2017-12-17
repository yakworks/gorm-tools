package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.model.MappingContext
import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

//import org.springframework.core.GenericTypeResolver

/**
 * Base event class for Dao firing
 * @param < D >
 */
@CompileStatic
class RepositoryEvent<D> extends ApplicationEvent implements ResolvableTypeProvider {//extends ApplicationEvent {//

    D entity
    String eventKey = "repoEvent"

    RepositoryEvent(final Datastore source, final D entity, String eventKey) {
        super(source)
        MappingContext mappingContext = source.getMappingContext()
        this.entity = mappingContext.getProxyHandler().unwrap(entity)
        this.eventKey = eventKey
        //this.entity = mappingContext.getPersistentEntity(entityObject.getClass().getName());
    }

    /**
     * done per spring docs so that listeners can bind to the generic of the event.
     * ex: implements ApplicationListener<BeforeCreateEvent<CityDaoSpringEventsRefreshable>>
     * or @EventListener
     *    void beforeCreate(BeforeCreateEvent<CityDaoSpringEvents> event)
     */
    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getEntity()))
    }

    /**
     * @return the routing key in the form of "DomainClass.eventMethod", for example "City.afterPersist"
     */
    String getRoutingKey() { "${entity.class.simpleName}.${eventKey}" }
}
