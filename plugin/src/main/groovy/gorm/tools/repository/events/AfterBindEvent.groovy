package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired After a bind and save/persist. Often used along with params to setup other domains associations
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBindEvent<D> extends RepositoryEvent<D> {

    AfterBindEvent(RepositoryApi repo, D entity, Map data, String bindAction) {
        super(repo, entity, RepositoryEventType.AfterBind.eventKey)
        this.data = data
        this.bindAction = bindAction
    }

}
