/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest

import groovy.transform.CompileDynamic

import grails.converters.JSON
import yakworks.rest.grails.AppInfoBuilder

//see http://plugins.grails.org/plugin/grails/spring-security-appinfo
// for app that shows app-info
@CompileDynamic
class AppInfoController {

    //injected
    AppInfoBuilder appInfoBuilder

    def meta() {
        render grailsApplication.metadata as JSON
    }

    def urlMappings() {
        render appInfoBuilder.urlMappings() as JSON
    }

    def memoryInfo() {
        render appInfoBuilder.memoryInfo() as JSON
    }

    def beanInfo() {
        render appInfoBuilder.beanInfo() as JSON
    }

}
