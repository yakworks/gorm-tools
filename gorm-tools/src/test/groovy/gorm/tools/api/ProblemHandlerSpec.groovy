package gorm.tools.api

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource

import gorm.tools.testing.support.GormToolsSpecHelper
import gorm.tools.testing.unit.GormAppUnitTest
import grails.testing.spring.AutowiredTest
import spock.lang.Specification
import yakworks.i18n.icu.DefaultICUMessageSource

import static yakworks.api.HttpStatus.INTERNAL_SERVER_ERROR
import static yakworks.api.HttpStatus.NOT_FOUND
import static yakworks.api.HttpStatus.UNPROCESSABLE_ENTITY

class ProblemHandlerSpec extends Specification implements GormAppUnitTest {

    MessageSource messageSource
    ProblemHandler problemHandler

    void setupSpec() {
        defineCommonBeans()
    }

    void "sanity check"(){
        expect:
        messageSource instanceof DefaultICUMessageSource
        problemHandler
        1 == 1.0
    }

    // def 'Unhandled Problem'() {
    //     when:
    //     def problem = problemHandler.handleException(new RuntimeException("test error"))
    //
    //     then:
    //     problem.status == INTERNAL_SERVER_ERROR
    //     problem.code == 'error.unhandled'
    //     // problem.title == 'Unhandled Problem'
    //     problem.detail == "test error"
    //     //XXX finish showing stacktrace
    // }

    void 'WTF'() {
        when:
        def rte = new RuntimeException("test error")
        // def problem = problemHandler.handleException(new RuntimeException("test error"))

        then:
        rte
    }
    //
    // def 'validation exception'() {
    //     when:
    //     def valEx
    //     try{
    //         new Cust(companyId: Company.DEFAULT_COMPANY_ID, type: OrgType.Customer).persist()
    //     }catch(e){
    //         valEx = e
    //     }
    //     assert valEx instanceof EntityValidationException
    //     def problem = problemHandler.handleException(valEx)
    //
    //     then:
    //     problem.statuc == 422
    //     problem.code == "validation.error"
    //     problem.title == "Org Validation Error(s)"
    //     problem.errors.size() == 2
    //     //XXX finish testing this
    //
    // }

}
