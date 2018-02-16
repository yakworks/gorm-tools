package gorm.tools.testing.integration

import grails.util.GrailsMetaClassUtils
import grails.util.GrailsWebMockUtil
import org.codehaus.groovy.runtime.HandleMetaClass
import org.grails.plugins.testing.GrailsMockHttpServletRequest
import org.grails.plugins.testing.GrailsMockHttpServletResponse
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

/**
 * Contains helpers for integration tests for controllers. Can be chained with some custom helper traits with the
 * application-specific initialization logic.
 *
 * The logic may be injected, using the standard Spock approach with @Before/@After, etc. JUnit annotations (Spock
 * conventional 'setup/cleanup/setupSpec/cleanupSpec' names do not work for traits).
 *
 * As an alternative, the gorm-tools provides the approach with auxiliary conventional methods
 * 'specificSetup/specificCleanup/specificSetupSpec/specificCleanupSpec' that should not be marked with any annotations.
 * That's useful for the case, when you don't want your code to depend on test dependencies.
 */
trait ControllerIntegrationSpecHelper extends IntegrationSpecHelper {

    /**
     * A web application context instance which is used for mocking the controller.
     */
    @Autowired
    WebApplicationContext ctx

    /**
     * Sets up mock request/response pair and performs a dynamic call to the 'specificSetup' method on the test class.
     */
    @Before
    void controllerIntegrationSetup() {
        MockHttpServletRequest request = new GrailsMockHttpServletRequest(ctx.servletContext)
        MockHttpServletResponse response = new GrailsMockHttpServletResponse()

        GrailsWebMockUtil.bindMockWebRequest(ctx, request, response)

        currentRequestAttributes.setControllerName(controllerName)

        super.doSpecificSetup()
    }

    /**
     * Resets request attributes in request holder after each test case
     */
    @After
    void controllerIntegrationCleanup() {
        RequestContextHolder.resetRequestAttributes()
    }

    /**
     * Can be overriden in the test class to return the controller name. This name is appended to the request attributes.
     * Null is just a default value.
     */
    String getControllerName() { null }

    /**
     * Returns the current request attributes
     */
    GrailsWebRequest getCurrentRequestAttributes() {
        (GrailsWebRequest)RequestContextHolder.currentRequestAttributes()
    }

    /**
     * Autowires bean properties of a given controller and returns the controller instance.
     *
     * @param controller a controller instance to be initialized
     */
    def autowire(def controller) {
        ctx.autowireCapableBeanFactory.autowireBeanProperties(controller, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        controller
    }

    /**
     * Adds mock of the 'render' method to a metaclass of a given controller.
     *
     * @param controller a controller to mock the render method for.
     */
    void mockRender(def controller) {
        MetaClass metaClass = GrailsMetaClassUtils.getMetaClass(controller)
        metaClass.render = { Map args ->
            GrailsMetaClassUtils.getMetaClass(controller).renderArgs = args
        }

        if(controller.metaClass instanceof HandleMetaClass) ((HandleMetaClass)controller.metaClass).replaceDelegate()
    }

    @Override
    void doSpecificSetup() {
        //Override this as an empty method to block the specificSetup call in the IntegrationSpecHelper.
        // Since the ControllerIntegrationSpecHelper initialization should happen first.
    }
}
