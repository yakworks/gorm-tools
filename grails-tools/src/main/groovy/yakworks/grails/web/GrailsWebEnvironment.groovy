/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.web

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.web.context.ServletEnvironmentGrailsApplicationDiscoveryStrategy
import org.grails.web.servlet.WrappedResponseHolder
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext
import org.springframework.context.i18n.LocaleContext
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.servlet.DispatcherServlet
import org.springframework.web.servlet.support.RequestContextUtils

import grails.util.GrailsWebMockUtil
import grails.util.Holders

/**
 * based on the RenderEnvironment in grails-rendering and private class in grails-mail
 * All this does is bind a mock request and mock response is one doesn't exist
 * deals with setting the WrappedResponseHolder.wrappedResponse as well
 */
@SuppressWarnings(['CompileStatic']) //FIXME there is a bug in the codenarc ext that looks at new LocaleContext() as a class
@Slf4j
@CompileStatic
class GrailsWebEnvironment implements AutoCloseable{

    final Writer out
    final Locale locale
    final ApplicationContext applicationContext

    private GrailsWebRequest originalRequestAttributes
    private GrailsWebRequest renderRequestAttributes

    GrailsWebEnvironment(ApplicationContext applicationContext, Writer out, Locale locale = null) {
        this.out = out
        this.locale = locale
        this.applicationContext = applicationContext
    }

    void initCopy() {
        originalRequestAttributes = RequestContextHolder.getRequestAttributes() as GrailsWebRequest

        def renderLocale = locale
        if (!renderLocale && originalRequestAttributes) {
            renderLocale = RequestContextUtils.getLocale(originalRequestAttributes.request)
        }
        renderRequestAttributes = (GrailsWebRequest)bindMockWebRequest(applicationContext, out, renderLocale)

        if (originalRequestAttributes) {
            renderRequestAttributes.controllerName = originalRequestAttributes.controllerName
        }
    }

    void close() {
        //TODO Investigate this -> RequestContextHolder.resetRequestAttributes()
        //see http://grails.1312388.n4.nabble.com/inheriting-thread-local-state-when-using-spring-beans-in-Grails-with-request-scope-td4426102.html
        //https://github.com/rawls238/Scientist4J/issues/12
        RequestContextHolder.setRequestAttributes(originalRequestAttributes) // null ok
        WrappedResponseHolder.wrappedResponse = originalRequestAttributes?.currentResponse
    }

    /**
     * Establish an environment inheriting the locale of the current request if there is one
     */
    static void withNew(ApplicationContext applicationContext, Writer out, Closure block) {
        withNew(applicationContext, out, null, block)
    }

    /**
     * Establish an environment with a specific locale
     */
    static void withNew(ApplicationContext applicationContext, Writer out, Locale locale, Closure block) {
        def env = new GrailsWebEnvironment(applicationContext, out, locale)
        env.initCopy()
        try {
            block(env)
        } finally {
            env.close()
        }
    }

    String getControllerName() {
        renderRequestAttributes.controllerName
    }

    static ServletWebRequest bindRequestIfNull() {

        return bindRequestIfNull(Holders.grailsApplication.mainContext)
    }

    static ServletWebRequest bindRequestIfNull(ApplicationContext appCtx) {
        return bindRequestIfNull(appCtx, new StringWriter())
    }

    static ServletWebRequest bindRequestIfNull(ApplicationContext appCtx, Writer out, Locale preferredLocale = null) {
        ServletWebRequest grailsWebRequest = (ServletWebRequest) RequestContextHolder.getRequestAttributes()
        if (grailsWebRequest) {
            //TODO unbindRequest = false
            log.debug("grailsWebRequest exists")
            return grailsWebRequest
        }

        return bindMockWebRequest(appCtx, out, preferredLocale)
    }

    @CompileDynamic
    static ServletWebRequest bindMockWebRequest(ApplicationContext appCtx, Writer wout, Locale preferredLocale = null) {
        //TODO unbindRequest = true
        log.debug("a mock grailsWebRequest is being bound")

        //from tests stuff TODO would be good to see if these are avliable and use them
        //def request = new GrailsMockHttpServletRequest()
        //def response = new GrailsMockHttpServletResponse()

        GrailsWebRequest grailsWebRequest = GrailsWebMockUtil.bindMockWebRequest(appCtx as WebApplicationContext)
        //setup locale on request and in LocaleContextHolder

        LocaleContextHolder.setLocaleContext(new LocaleContext() {
            Locale getLocale() {
                return appCtx.localeResolver.resolveLocale(grailsWebRequest.request)
            }
        })
        //LOCALE_RESOLVER_ATTRIBUTE(request attr) LOCALE_RESOLVER_BEAN_NAME(localResolver) LOCALE_SESSION_ATTRIBUTE_NAME()
        MockHttpServletRequest request = grailsWebRequest.request as MockHttpServletRequest
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, appCtx.localeResolver)
        request.addPreferredLocale(preferredLocale ?: Locale.default)
        //setup contextPath so tags like resouce and linkTo work
        request.contextPath = (appCtx as WebApplicationContext).servletContext.contextPath
        //setup the default out
        grailsWebRequest.setOut(wout)
        //holders
        if (!Holders.servletContext) Holders.servletContext = grailsWebRequest.servletContext
        Holders.addApplicationDiscoveryStrategy(new ServletEnvironmentGrailsApplicationDiscoveryStrategy(grailsWebRequest.servletContext))
        grailsWebRequest.servletContext.setAttribute(GrailsApplicationAttributes.APPLICATION_CONTEXT, appCtx)
        //WrappedResponseHolder
        WrappedResponseHolder.wrappedResponse = grailsWebRequest.currentResponse
        return grailsWebRequest
    }
}
