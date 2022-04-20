package grails.plugin.viewtools

import foobar.DemoController
import foobar.TenantViewResourceLoader
import grails.testing.mixin.integration.Integration
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.core.io.Resource
import org.springframework.util.StringUtils
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.grails.web.GrailsWebEnvironment
import yakworks.grails.web.ViewResourceLocator

//import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
//import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
@Integration
//@Rollback
class ViewResourceLocatorIntegrationSpec extends Specification  {

    ViewResourceLocator viewResourceLocator
    DemoController controller
    def grailsApplication

    StringWriter writer = new StringWriter()

    def setup() {
//        GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext,writer)
//        controller = new foobar.FooPluginController()
//        controller.viewResourceLocator = viewResourceLocator
    }

     def cleanup() {
     }

    void "simple find"() {
        //the classpath:customeviews should be setup in the bean
        when:
        Resource res = viewResourceLocator.locate('/foo/index.md')

        then:
        assert res
        res.getURI().toString().endsWith("/foo/index.md")

    }

     void "find override on external search path"() {
         //the classpath:customeviews should be setup in the bean
         when:
         Resource res = viewResourceLocator.locate('/foo/override.md.ftl')

         then:
         assert res
         res.getURI().toString().endsWith("view-templates/foo/override.md.ftl")

     }

     void "test classpath conf/customviews/customview.hbr"() {
         //the classpath:customeviews should be setup in the bean
         when:
         Resource res = viewResourceLocator.locate('testAppViewToolsGrailsAppConf.hbr')

         then:
         assert res
         res.getURI().toString().endsWith( "testAppViewToolsGrailsAppConf/testAppViewToolsGrailsAppConf.hbr")

     }

    void "view in plugin from classpath or full scan"() {
        when:
        Resource res = viewResourceLocator.locate('/fooPlugin/index.md')

        then:
        //its a full scan in grails 2
        assert res?.exists()
        //works in grails3
        //res.getURI().toString().endsWith( "classpath:/fooPlugin/index.md")
        assert viewResourceLocator.locate('spock/lang/Specification.class')
    }

    void "view with plugin controller in request"() {
        when:
        //println Environment.grailsVersion
        grailsApplication.mainContext.getBeanDefinitionNames().each {
            println it
        }
        GrailsWebEnvironment.bindRequestIfNull(grailsApplication.mainContext, writer)
        def controller = grailsApplication.mainContext.getBean("foobar.FooPluginController")
        def request = GrailsWebRequest.lookup()?.getCurrentRequest()
        request.setAttribute(GrailsApplicationAttributes.CONTROLLER, controller)
        //controller.viewResourceLocator = viewResourceLocator
        Resource res = viewResourceLocator.locate('/fooPlugin/index.md')

        then:
        String uri = StringUtils.cleanPath(res.getURI().toString())
        String toCompare = "grails-tools-plugin/grails-app/views/fooPlugin/index.md"
        String toCompare2 = "grails-tools-plugin-0.1.jar!/fooPlugin/index.md"
        uri.endsWith( toCompare) || uri.endsWith( toCompare2)
    }

    @Ignore
    void "view using plugin path"() {
        when:
        //controller.viewResourceLocator = viewResourceLocator
        Resource res = viewResourceLocator.locate('/plugins/grails-tools-plugin-0.1/fooPlugin/index.md')

        then:
        res.exists()
        String uri = StringUtils.cleanPath(res.getURI().toString())
        String toCompare = "grails-tools-plugin/grails-app/views/fooPlugin/index.md"
        String toCompare2 = "grails-tools-plugin-0.1.jar!/fooPlugin/index.md"
        uri.endsWith( toCompare) || uri.endsWith( toCompare2)
    }

    void "tenantA"() {
        when:
        TenantViewResourceLoader.currentTenant.set('tenantA')
        //controller.viewResourceLocator = viewResourceLocator
        Resource res = viewResourceLocator.locate('/foo/tenantA.ftl')

        then:
        res.exists()
        res.getFile().text.contains('tenantA got it')
        TenantViewResourceLoader.currentTenant.set('tenantB')
        assert viewResourceLocator.locate('tenantB.hbs').exists()
    }

    void "view under ConfigKeyAppResourceLoader"() {
        when:
        Resource res = viewResourceLocator.locate('appResourceView.md')

        then:
        res.exists()
        res.file.exists()
        res.file.absolutePath.endsWith("root-location/views/appResourceView.md")
    }


}
