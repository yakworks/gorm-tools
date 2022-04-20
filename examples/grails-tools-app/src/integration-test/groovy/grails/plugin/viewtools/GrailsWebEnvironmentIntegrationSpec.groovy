package grails.plugin.viewtools

import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import org.grails.web.servlet.WrappedResponseHolder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder

//import org.codehaus.groovy.grails.commons.GrailsApplication
//import org.codehaus.groovy.grails.web.servlet.WrappedResponseHolder
import spock.lang.Specification
import yakworks.grails.web.GrailsWebEnvironment

//import grails.test.spock.IntegrationSpec

@Integration
//@Rollback
class GrailsWebEnvironmentIntegrationSpec extends Specification  {

    //@Autowired
    GrailsApplication grailsApplication

    def setup() {
        GrailsWebEnvironment.bindRequestIfNull()
    }

    def cleanup() {
    }

    void "test it already exists"() {
        expect:
        RequestAttributes grailsWebRequest = RequestContextHolder.getRequestAttributes()
        assert grailsWebRequest
    }

    void "test bindRequestIfNull"() {
        when: "null requests and response"
        RequestContextHolder.setRequestAttributes(null);
        WrappedResponseHolder.wrappedResponse = null
        then: "assert its null"
        RequestAttributes grailsWebRequest = RequestContextHolder.getRequestAttributes()
        assert !grailsWebRequest

        when: "bindRequestIfNull"
        Writer writer = new CharArrayWriter()
        GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext, writer)

        then: "request and response should exist"
        assert RequestContextHolder.getRequestAttributes().request instanceof MockHttpServletRequest
        assert WrappedResponseHolder.wrappedResponse


    }

}
