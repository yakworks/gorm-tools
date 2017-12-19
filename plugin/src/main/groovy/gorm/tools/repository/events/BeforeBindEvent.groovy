package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired before a bind and save/persist. Allows modification to params used in the binding
 *
 * @param D the entity domain class
 */
@CompileStatic
class BeforeBindEvent<D> extends RepositoryEvent<D> {

    BeforeBindEvent(Datastore source, D entity, Map data, String bindAction) {
        super(source, entity, RepositoryEventType.BeforeBind.eventKey)
        this.data = data
        this.bindAction = bindAction
    }

}
