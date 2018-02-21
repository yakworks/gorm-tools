package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

@CompileStatic
class ErrorEvent<D> extends RepositoryEvent<D> {

    RuntimeException exception

    ErrorEvent(RepositoryApi repo, D entity, Map data, RuntimeException exception) {
        super(repo, entity, RepositoryEventType.OnError.eventKey)
        this.data = data
        this.exception = exception
    }

    String getBindAction(){
        data ? data['bindAction'] as String : null
    }
}
