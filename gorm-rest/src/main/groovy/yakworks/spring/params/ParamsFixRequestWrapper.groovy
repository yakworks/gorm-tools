/*
* Copyright 2004-2005 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.params


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * Wraps the request and re-parses the query string when the params are empty.
 */
@Slf4j
@CompileStatic
class ParamsFixRequestWrapper extends HttpServletRequestWrapper {

    Map<String, String[]> cachedParams

    /**
     * Creates a new instance
     * @param request the original {@link HttpServletRequest}
     */
    ParamsFixRequestWrapper(HttpServletRequest request) {
        super(request)
    }

    private HttpServletRequest getHttpRequest() {
        return (HttpServletRequest) super.getRequest();
    }

    @Override
    String toString() {
        return "ParamsFixRequestWrapper[ " + getRequest() + "]"
    }

    /**
     * The default behavior of this method is to return getParameterMap() on the wrapped request object.
     * We check to see if the original request has a queryString because that doesnt get lost and if there are no
     * params we reparse them
     */
    @Override
    Map<String, String[]> getParameterMap() {
        //['foo': ['over'], 'bar': ['under']] as Map<String, String[]>
        // return this.request.getParameterMap()
        ensureParams()
        return cachedParams
    }


    /**
     * goes through getParameterMap()
     */
    @Override
    String getParameter(String name) {
        String[] arr = this.getParameterMap().get(name)
        return (arr != null && arr.length > 0 ? arr[0] : null)
    }

    /**
     * goes through getParameterMap()
     */
    @Override
    Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet())
    }


    /**
     * goes through getParameterMap()
     */
    @Override
    String[] getParameterValues(String name) {
        return getParameterMap().get(name)
    }

    void ensureParams(){
        if(cachedParams) return

        //possibly from an async operation, still investigating, the params in the request get lost or dropped,
        // but queryString still there
        if(!hasRequestParameters() && httpRequest.queryString) {
            cachedParams = QueryParamsUtil.parseQueryString(httpRequest.queryString)
            logDetails()
        } else {
            cachedParams = httpRequest.getParameterMap()
        }
    }

    boolean hasRequestParameters(){
        //always return false if we want to test with the parser
        //false
        httpRequest.getParameterMap()
    }

    @SuppressWarnings('LineLength')
    void logDetails(){
        if(log.isWarnEnabled()) {
            String msg = """
                ⚠️ - SPRING LOST PARAMS - REPARSED - ⚠️
                queryString=[${httpRequest.queryString}] , method=[${httpRequest.method}] , requestURI=[${httpRequest.requestURI}], contentType:[${httpRequest.getContentType()}]
            """.stripIndent()
            //msg + "\n  params parsed from queryString - ${parsedParams}"
            log.warn(msg)
        }
    }

}
