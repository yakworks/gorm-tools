/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

import yakworks.spring.CustomRequestMappingHandlerAdapter

// @EnableWebMvc <- Remove annotation and extend from DelegatingWebMvcConfiguration directly
@Configuration(proxyBeanMethods = false)
@CompileStatic
class WebMvcConfiguration extends DelegatingWebMvcConfiguration {
    // @Override
    // RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
    //     return new CustomRequestMappingHandlerMapping();
    // }

    @Override
    RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter handlerAdapter = new CustomRequestMappingHandlerAdapter();
        //handlerMapping.setUseTrailingSlashMatch(false);
        return handlerAdapter;
    }
}
