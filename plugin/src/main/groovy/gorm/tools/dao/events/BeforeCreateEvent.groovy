package gorm.tools.dao.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired before a bind and save/persist. Allows modification to params used in the binding
 *
 * @param D the entity domain class
 */
@CompileStatic
class BeforeCreateEvent<D> extends DaoEvent<D> {

    Map params

    BeforeCreateEvent(Datastore source, D entity, Map params) {
        super(source, entity, DaoEventType.BeforeCreate.eventKey)
        this.params = params
    }

}
