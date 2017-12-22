package gorm.tools.repository.errors

import gorm.tools.repository.RepoMessage
import org.grails.datastore.gorm.GormEntity
import org.springframework.dao.DataAccessException
import org.springframework.dao.DataIntegrityViolationException

/**
 * Handler for exceptions thrown by the Repository
 */
class RepoExceptionSupport {

    RuntimeException translateException(RuntimeException ex, GormEntity entity) {
        if (ex instanceof grails.validation.ValidationException) {
            grails.validation.ValidationException ve = (grails.validation.ValidationException) ex
            return new EntityValidationException(RepoMessage.notSaved(entity), entity, ve.errors, ve)
        } else if (ex instanceof DataIntegrityViolationException) {
            //see http://www.baeldung.com/spring-dataIntegrityviolationexception
            String ident = RepoMessage.badge(entity.ident(), entity)
            //log.error("repository delete error on ${entity.id} of ${entity.class.name}",dae)
            return new EntityValidationException(RepoMessage.notDeleted(entity, ident), entity, ex)
        } else if (ex instanceof DataAccessException) {
            //for now just return it. TODO we need to look into the OptimisticLocking and how to handle that here.
            return new EntityValidationException(RepoMessage.notSaved(entity), entity, ex) //make a RepoMessage.notSavedDataAccess
        }

    }
}
