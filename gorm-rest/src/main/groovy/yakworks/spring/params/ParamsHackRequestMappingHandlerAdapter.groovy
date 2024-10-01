/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring.params

import groovy.transform.CompileStatic

import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod

/**
 * overrides to provide the the ParamsFixWebRequest to the ServletInvocableHandlerMethod which does most of the Annotations work
 * setting up and calling the controller methods.
 */
@CompileStatic
class ParamsHackRequestMappingHandlerAdapter extends RequestMappingHandlerAdapter {

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
