package gorm.tools.repository.errors

import gorm.tools.repository.RepoMessage
import groovy.transform.CompileStatic
import org.springframework.orm.ObjectOptimisticLockingFailureException

@CompileStatic
class EntityOptimisticLockingException extends ObjectOptimisticLockingFailureException {
    String message
    Object entity
    Map messageMap

    EntityOptimisticLockingException(Object entity, ex) {
        super(entity.class, entity["id"] as Serializable, ex as Throwable)
        this.messageMap = RepoMessage.optimisticLockingFailure(entity)
        this.message = messageMap.defaultMessage
        this.entity = entity
    }
}
