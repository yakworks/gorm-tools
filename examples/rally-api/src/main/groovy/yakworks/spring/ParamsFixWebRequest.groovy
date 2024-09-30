/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import javax.servlet.ServletRequest
import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileStatic

import org.springframework.lang.Nullable
import org.springframework.util.CollectionUtils
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest

/**
 * Wraps the servletWebRequest and then wraps the HttpServletRequest
 * so it can re-parses the query string when the params are empty.
 */
@CompileStatic
class ParamsFixWebRequest implements NativeWebRequest {
    @Delegate ServletWebRequest servletWebRequest

    private final HttpServletRequest fixRequest

    ParamsFixWebRequest(NativeWebRequest nativeWebRequest){
        this.servletWebRequest =  (ServletWebRequest) nativeWebRequest
        this.fixRequest = new ParamsFixRequestWrapper(servletWebRequest.request)
    }

    /**
     * Return the underlying native request object, if available.
     * @param requiredType the desired type of request object
     * @return the matching request object, or {@code null} if none
     * of that type is available
     * @see javax.servlet.http.HttpServletRequest
     */
    @Override //override delegate to
    <T> T getNativeRequest(@Nullable Class<T> requiredType){
        if(ServletRequest.isAssignableFrom(requiredType)){
            return (T) fixRequest
        } else {
            return servletWebRequest.getNativeRequest(requiredType)
        }
    }

    /**
     * The default behavior of this method is to return getParameterMap() on the wrapped request object.
     * Some parts of spring mvc like the RequestParamMethodArgumentResolver that handles the @RequestParam
     * use this method in the  servletWebRequest to resolve.
     * Others like the @ModelAttributes will get the ServletRequest and use that directly which is why we wrap the request
     * and dont just onverride this. So we need both.
     */
    @Override
    Map<String, String[]> getParameterMap() {
        return fixRequest.getParameterMap()
    }

    @Override
    String getParameter(String paramName) {
        return fixRequest.getParameter(paramName)
    }

    @Override
    String[] getParameterValues(String paramName) {
        return fixRequest.getParameterValues(paramName)
    }

    @Override
    Iterator<String> getParameterNames() {
        return CollectionUtils.toIterator(fixRequest.getParameterNames())
    }
}
