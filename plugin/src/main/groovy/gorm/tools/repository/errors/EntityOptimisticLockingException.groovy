package gorm.tools.repository.errors

import gorm.tools.repository.RepoMessage
import groovy.transform.CompileStatic
import org.hibernate.StaleObjectStateException
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException


@CompileStatic
class EntityOptimisticLockingException extends HibernateOptimisticLockingFailureException {
    String message
    Object entity
    Map messageMap

    EntityOptimisticLockingException(StaleObjectStateException ex) {
        super(ex)
    }

    EntityOptimisticLockingException(Object entity) {
        super(new StaleObjectStateException(entity.class.name, entity["id"] as Serializable))
        messageMap = RepoMessage.optimisticLockingFailure(entity)
        this.message = messageMap.defaultMessage
        this.entity = entity
    }
}
