/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing

import groovy.transform.CompileDynamic

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

import gorm.tools.testing.integration.DataIntegrationTest
import grails.util.GrailsMetaClassUtils
import grails.util.GrailsWebMockUtil

/**
 * Contains helpers for integration tests for controllers. Can be chained with some custom helper traits with the
 * application-specific initialization logic.
 */
@CompileDynamic //ok for testing
trait ControllerIntTest extends DataIntegrationTest {

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
    def autowire(Object controller) {
        ctx.autowireCapableBeanFactory.autowireBeanProperties(controller, AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        controller
    }

    /**
     * Adds mock of the 'render' method to a metaclass of a given controller.
     *
     * @param controller a controller to mock the render method for.
     */
    void mockRender(Object controller) {
        MetaClass metaClass = GrailsMetaClassUtils.getMetaClass(controller)
        metaClass.render = { Map args ->
            GrailsMetaClassUtils.getMetaClass(controller).renderArgs = args
        }

        if(controller.metaClass instanceof HandleMetaClass) ((HandleMetaClass)controller.metaClass).replaceDelegate()
    }

}
