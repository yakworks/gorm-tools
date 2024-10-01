/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

import yakworks.spring.params.ParamsHackRequestMappingHandlerAdapter

/**
 * Params Hack
 * @EnableWebMvc <- Need to remove the EnableWebMvc annotation and extend from DelegatingWebMvcConfiguration directly.
 * EnableWebMvc imports the DelegatingWebMvcConfiguration so cant do both.
 * With Grail this means that we can't use the normal grails-app/init/Application class since the transformation adds the EnableWebMvc
 */
@Configuration(proxyBeanMethods = false)
@CompileStatic
class WebMvcConfiguration extends DelegatingWebMvcConfiguration {

    @Override
    RequestMappingHandlerAdapter createRequestMappingHandlerAdapter() {
        return new ParamsHackRequestMappingHandlerAdapter()
    }
}
