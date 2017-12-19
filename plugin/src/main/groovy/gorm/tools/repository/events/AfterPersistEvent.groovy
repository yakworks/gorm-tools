package gorm.tools.repository.events

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.core.Datastore

/**
 * Fired after successful repository.persist
 */
@CompileStatic
class AfterPersistEvent<D> extends RepositoryEvent<D> {

    /** the args passed into persist */
    Map args

    AfterPersistEvent(Datastore source, D entity) {
        super(source, entity, RepositoryEventType.AfterPersist.eventKey)
    }

    AfterPersistEvent(Datastore source, D entity, Map args) {
        super(source, entity, RepositoryEventType.AfterPersist.eventKey)
        this.args = args
        setDataFromArgMap(args)
    }

}
