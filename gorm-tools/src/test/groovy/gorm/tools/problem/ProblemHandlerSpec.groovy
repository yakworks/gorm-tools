package gorm.tools.problem

import org.springframework.context.MessageSource
import spock.lang.Specification
import yakworks.i18n.icu.DefaultICUMessageSource
import yakworks.testing.gorm.unit.DataRepoTest

class ProblemHandlerSpec extends Specification implements DataRepoTest {

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
    //     //FIXME finish showing stacktrace
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
    //     //FIXME finish testing this
    //
    // }

}
