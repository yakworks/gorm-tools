/*
* Copyright 2013-2015 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.shiro

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.apache.shiro.authz.AuthorizationException
import org.apache.shiro.authz.UnauthenticatedException
import org.grails.core.exceptions.GrailsRuntimeException
import org.grails.web.errors.GrailsExceptionResolver
import org.grails.web.sitemesh.GrailsContentBufferingResponse
import org.springframework.web.servlet.ModelAndView

import grails.web.mapping.UrlMappingInfo
import grails.web.mapping.UrlMappingsHolder

import static org.springframework.http.HttpStatus.FORBIDDEN

@CompileStatic
@Slf4j
class ShiroGrailsExceptionResolver extends GrailsExceptionResolver {

    /**
     * Override so we can redirect when error matches the auth exception it direct to view handling
     * done as one reason the default resolveException logs out a bunch of messages that we dont want to poolute the log
     */
    @Override
    ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Exception authEx = findAuthException(ex)
        if (authEx) {
            return resolveAuthorizationException(request, response, handler, authEx)
        } else {
            return super.resolveException(request, response, handler, ex)
        }

    }

    ModelAndView resolveAuthorizationException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
        request.session["Authmsg"] = ex.message
        prepareResponse(ex, response)
        ModelAndView mv = doResolveException(request, response, handler, ex)
        mv.status = FORBIDDEN
        log.debug("Resolved [" + ex + "]" + (mv.isEmpty() ? "" : " to " + mv))
        UrlMappingsHolder urlMappings = lookupUrlMappings();
        if (urlMappings != null) {
            mv = resolveByViewPathOrStatus(ex, urlMappings, request, response, mv)
        }
        return mv
    }

    /**
     * Looks in UrlMappings for url path that matches the ModelAndView's viewName and
     * direct to the view or controller setup there.
     */
    protected ModelAndView resolveByViewPathOrStatus(Exception ex, UrlMappingsHolder urlMappings, HttpServletRequest request,
                                                HttpServletResponse response, ModelAndView mv) {

        UrlMappingInfo info = urlMappings.match(mv.viewName)
        if(!info) info = urlMappings.matchStatusCode(mv.status.value())

        try {
            if (info != null && info.getViewName() != null) {
                resolveView(request, info, mv);
            }
            else if (info != null && info.getControllerName() != null) {
                String uri = determineUri(request);
                if (!response.isCommitted()) {
                    if(response instanceof GrailsContentBufferingResponse) {
                        // clear the output from sitemesh before rendering error page
                        ((GrailsContentBufferingResponse)response).deactivateSitemesh();
                    }
                    forwardRequest(info, request, response, mv, uri);
                    // return an empty ModelAndView since the error handler has been processed
                    return new ModelAndView();
                }
            }
            return mv;
        }
        catch (Exception e) {
            LOG.error("Unable to render errors view: " + e.getMessage(), e);
            throw new GrailsRuntimeException(e);
        }
    }

    static Exception findAuthException(Exception e) {
        if (e instanceof AuthorizationException) {
            return e
        }
        //Note order is important Unauthenticated comes before Authorization
        Throwable parentThrowable = e
        while (parentThrowable.getCause() && parentThrowable != parentThrowable.getCause()) {

            Throwable childThrowable = parentThrowable.getCause()
            if (childThrowable instanceof UnauthenticatedException) {
                return (Exception) childThrowable
            }
            if (childThrowable instanceof AuthorizationException) {
                return (Exception) childThrowable
            }
            parentThrowable = childThrowable
        }
        return null
    }
}
