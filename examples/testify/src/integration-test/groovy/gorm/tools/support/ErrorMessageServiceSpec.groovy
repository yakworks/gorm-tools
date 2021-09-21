package gorm.tools.support

import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification
import yakworks.rally.activity.model.ActivityNote
import yakworks.rally.orgs.model.Company
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgType

@Integration
@Rollback
class ErrorMessageServiceSpec extends Specification implements DataIntegrationTest {

    @Autowired
    ErrorMessageService errorMessageService

    def testUnknownException() {
        when:
        def error = errorMessageService.buildErrorResponse(new RuntimeException("test error"))

        then:
        error.code == 500
        error.status == "error"
        error.messageCode == 0
        error.message == "test error"
        error.errors == [:]

    }

    def testDomainException() {
        when:
        def domainException
        try{
            new Org(companyId: Company.DEFAULT_COMPANY_ID, type: OrgType.Customer).persist()
        }catch(e){
            domainException = e
        }
        def error = errorMessageService.buildErrorResponse(domainException)

        then:
        error.code == 422
        error.status == "error"
        error.messageCode == "validation.error"
        error.message == "Org Validation Error(s)"
        error.errors.org.size() == 2
        error.errors.org.num == "Property [num] of class [class yakworks.rally.orgs.model.Org] cannot be null"
        error.errors.org.name == "Property [name] of class [class yakworks.rally.orgs.model.Org] cannot be null"

    }

    def testValidationException() {
        when:
        def validationException
        try{
            new ActivityNote().persist(flush: true)
        }catch(e){
            validationException = e
        }
        def error = errorMessageService.buildErrorResponse(validationException)

        then:
        error.code == 422
        error.status == "error"
        error.messageCode == 'validation.error'
        error.message == "ActivityNote Validation Error(s)"
        error.errors.activityNote.body ==  "Property [body] of class [class yakworks.rally.activity.model.ActivityNote] cannot be null"
    }

}
