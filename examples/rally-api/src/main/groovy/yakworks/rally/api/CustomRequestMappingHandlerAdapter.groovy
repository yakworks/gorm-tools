/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@CompileStatic
class CustomRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

    /**
     * Invoke the {@link RequestMapping} handler method preparing a {@link ModelAndView}
     * if view resolution is required.
     * @since 4.2
     * @see #createInvocableHandlerMethod(HandlerMethod)
     */
    //@Nullable
    @Override
    protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
                                               HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        super.invokeHandlerMethod(request, response, handlerMethod)
    }
}
