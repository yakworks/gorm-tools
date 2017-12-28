package gorm.tools.repository.errors

import gorm.tools.repository.RepoMessage
import org.grails.datastore.gorm.GormEntity
import org.hibernate.ObjectNotFoundException
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.OptimisticLockingFailureException

/**
 * Handler for exceptions thrown by the Repository
 */
class RepoExceptionSupport {

    RuntimeException translateException(RuntimeException ex, GormEntity entity) {
        if (ex instanceof grails.validation.ValidationException) {
            grails.validation.ValidationException ve = (grails.validation.ValidationException) ex
            return new EntityValidationException(RepoMessage.notSaved(entity), entity, ve.errors, ve)
        }else if (ex instanceof ObjectNotFoundException) {
            return new EntityNotFoundException(ex.identifier, ex.entityName)
        } else if (ex instanceof DataIntegrityViolationException) {
            //see http://www.baeldung.com/spring-dataIntegrityviolationexception
            return new EntityValidationException(ex.message, entity, ex.errors, ex)
        } else if (ex instanceof OptimisticLockingFailureException) {
            return new EntityOptimisticLockingException(entity)
        } else if (ex instanceof DataAccessException) {
            return new EntityValidationException(RepoMessage.notSavedDataAccess(entity), entity, ex)
        }
    }
}
