package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired After a bind and save/persist. Often used along with params to setup other domains associations
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBindEvent<D> extends RepositoryEvent<D> {

    AfterBindEvent(Datastore source, D entity, Map data, String bindAction) {
        super(source, entity, RepositoryEventType.AfterBind.eventKey)
        this.data = data
        this.bindAction = bindAction
    }

}
