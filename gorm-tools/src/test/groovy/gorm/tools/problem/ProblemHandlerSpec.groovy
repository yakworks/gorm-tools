package gorm.tools.problem

import groovy.json.JsonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.Errors
import spock.lang.Specification
import testing.Cust
import yakworks.api.HttpStatus
import yakworks.api.problem.GenericProblem
import yakworks.api.problem.Problem
import yakworks.i18n.icu.DefaultICUMessageSource
import yakworks.testing.gorm.unit.DataRepoTest

//FIXME beef this out, it appears to be only test?
class ProblemHandlerSpec extends Specification implements DataRepoTest {
    static List entityClasses = [Cust]

    // MessageSource messageSource
    @Autowired ProblemHandler problemHandler

    void setupSpec() {
        defineCommonGormBeans()
    }

    void "sanity check"(){
        expect:
        messageSource instanceof DefaultICUMessageSource
        problemHandler
        1 == 1.0
    }

    def 'Unhandled Problem'() {
        when:
        def problem = problemHandler.handleException(new RuntimeException("test error"))

        then:
        problem.status.code == 500
        problem.code == 'error.unexpected'
        // problem.title == 'Unhandled Problem'
        problem.detail == "test error"
        //FIXME finish showing stacktrace
    }

    void 'WTF'() {
        when:
        def rte = new RuntimeException("test error")
        // def problem = problemHandler.handleException(new RuntimeException("test error"))

        then:
        rte
    }

     void 'validation exception'() {
         when:
         new Cust(name2: "Test").persist(failOnError:true) //would fail constraints

         then:
         ValidationProblem.Exception ex = thrown()

         when:
         def problem = problemHandler.handleException(ex)

         then:
         problem instanceof ValidationProblem
         problem.status == HttpStatus.UNPROCESSABLE_ENTITY
         problem.code == "validation.problem"
         problem.title == "Validation Error(s)"

         when:
         Errors errors =  ((ValidationProblem)problem).errors

         then:
         errors != null
         errors.errorCount == 2
         errors.hasFieldErrors("name")
         errors.hasFieldErrors('type')
     }

    void "test is broken pipe"() {
        expect:
        !ProblemHandler.isBrokenPipe(new RuntimeException("test"))
        ProblemHandler.isBrokenPipe(new IOException("broken pipe"))
        ProblemHandler.isBrokenPipe(new IOException("java.io.IOException: Broken pipe"))
    }

    void "json exception"() {
        when:
        Problem p = problemHandler.handleException(new JsonException("test"))

        then:
        p instanceof GenericProblem
        p.cause instanceof JsonException

        when:
        p = problemHandler.handleException(new HttpMessageNotReadableException("test"))

        then:
        p instanceof GenericProblem
        p.cause instanceof HttpMessageNotReadableException
    }

}
