package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired right before enitity save inside repository.persist
 */
@CompileStatic
class BeforePersistEvent<D> extends RepositoryEvent<D> {

    /** the args passed into persist */
    Map args

    BeforePersistEvent(Datastore source, D entity) {
        super(source, entity, RepositoryEventType.BeforePersist.eventKey)
    }

    BeforePersistEvent(Datastore source, D entity, Map args) {
        super(source, entity, RepositoryEventType.BeforePersist.eventKey)
        this.args = args
        setDataFromArgMap(args)
    }

}
