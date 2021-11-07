/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.http

import groovy.transform.CompileDynamic

import org.codehaus.groovy.runtime.HandleMetaClass
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.junit.After
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder

import gorm.tools.beans.AppCtx
import gorm.tools.testing.integration.DataIntegrationTest
import grails.core.GrailsApplication
import grails.util.GrailsMetaClassUtils
import grails.util.GrailsNameUtils
import grails.util.GrailsWebMockUtil

/**
 * Contains helpers for integration tests for controllers. Can be chained with some custom helper traits with the
 * application-specific initialization logic.
 */
@CompileDynamic //ok for testing
trait RestIntegrationTest extends DataIntegrationTest {

    // Object controller
    String controllerName

    /**
     * A web application context instance which is used for mocking the controller.
     */
    @Autowired
    WebApplicationContext ctx

    @Autowired
    GrailsApplication grailsApplication

    /**
     * Sets up mock request/response pair and performs a dynamic call to the 'specificSetup' method on the test class.
     */
    @Before
    void controllerIntegrationSetup() {
        MockRestRequest request = new MockRestRequest(ctx.servletContext)
        MockRestResponse response = new MockRestResponse()
        GrailsWebMockUtil.bindMockWebRequest(ctx, request, response)
        currentRequestAttributes.setControllerName(controllerName)
    }

    MockRestResponse getResponse(){
        (MockRestResponse) controller.response
    }

    MockRestRequest getRequest(){
        (MockRestRequest) controller.request
    }

    // set the controller bean from short name, such as OrgController.
    void setControllerName(String name){
        def ctrls = grailsApplication.getArtefactInfo(ControllerArtefactHandler.TYPE).grailsClasses

        def grailsCtrlClass = ctrls.find{it.name == name || it.shortName == name}
        assert grailsCtrlClass : "can't find controller name $name"
        controller = AppCtx.get(grailsCtrlClass.getClazz())
        controllerName = grailsCtrlClass.getLogicalPropertyName()
        assert controller
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
     * Returns the current request attributes
     */
    GrailsWebRequest getCurrentRequestAttributes() {
        (GrailsWebRequest)RequestContextHolder.currentRequestAttributes()
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
