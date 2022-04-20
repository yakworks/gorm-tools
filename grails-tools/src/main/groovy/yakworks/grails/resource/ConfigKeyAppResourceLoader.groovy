/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.grails.resource

import groovy.transform.CompileStatic

import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader

import yakworks.commons.lang.Validate

/**
 * ConfigKeyAppResourceLoader provides ability to load resources from a directory configured as app resource location.
 */
@CompileStatic
class ConfigKeyAppResourceLoader implements ResourceLoader  {

    /**
     * Config key for app resource directory which holds the resources. eg views.location
     */
    String baseAppResourceKey

    AppResourceLoader appResourceLoader

    void setBaseAppResourceKey(String key) {
        Validate.notEmpty(key)
        baseAppResourceKey = "config:" + key
    }

    @Override
    Resource getResource(String uri) {
        return appResourceLoader.getResourceRelative(baseAppResourceKey, uri)
    }

    @Override
    ClassLoader getClassLoader() {
        return null
    }

}
