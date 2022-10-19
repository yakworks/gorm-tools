/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.config

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.YamlMapFactoryBean
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.stereotype.Component

@Component
@CompileStatic
class ApiProperties {
    /**
     * Grab value from config to load and split in arrays
     * this allows for resources like the following
     */// classpath*:/api/**/\*.yml
    @Value('${api.config.import}')
    private List apiResources;

    /** Full loaded yaml */
    Map<String, Object> api

    String defaultPackage
    Map<String, String> namespaces
    Map<String, Object> paths

    @PostConstruct
    void init() {
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean()
        if(!apiResources) return
        yamlMapFactoryBean.setResources(getResources(apiResources))
        Map<String, Object> map = yamlMapFactoryBean.getObject()
        api = (Map<String, Object>) map.get("api")
        if(!api) return
        defaultPackage = api['defaultPackage'] as String
        namespaces = api['namespaces'] as Map<String, String>
        paths = api['paths'] as Map<String, Object>
    }

    Resource[] getResources(List<String> apiResources){
        ResourceLoader defaultResourceLoader = new DefaultResourceLoader()
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(defaultResourceLoader)
        List rezs = [] as List<Resource>
        apiResources.each {
            def resourceList = resolver.getResources(it) as List
            rezs.addAll(resourceList)
        }
        rezs as Resource[]
    }
}
