/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileDynamic

import grails.web.servlet.mvc.GrailsParameterMap

/**
 * Legacy, should not be used for future dev
 */
@CompileDynamic
class GrailsParamMap {


    static GrailsParameterMap getGrailsParameterMap(Map p, HttpServletRequest request) {
        GrailsParameterMap gpm = new GrailsParameterMap(p, request)
        gpm.updateNestedKeys(p)
        return gpm
    }
}
