/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileStatic

import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod

@CompileStatic
class CustomRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {


    // @Override
    // protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
    //                                            HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
    //     super.invokeHandlerMethod(request, response, handlerMethod)
    // }

    @Override
    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new ServletInvocableHandlerMethod(handlerMethod){

            @Override
            Object invokeForRequest(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {
                var paramsFixWebRequest = new ParamsFixWebRequest(request)
                return super.invokeForRequest(paramsFixWebRequest, mavContainer, providedArgs)
            }
        }
    }
}
