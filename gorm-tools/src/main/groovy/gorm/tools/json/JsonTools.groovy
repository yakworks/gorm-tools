/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json

import groovy.json.JsonGenerator
import groovy.json.JsonOutput
import groovy.transform.CompileStatic

import gorm.tools.beans.EntityMap

/**
 * A helper class that uses the logic in grails json view plugin to generate json without needing
 * a gson file and outside the scope of and HTTP request using the JsonViewTemplateEngine
 * see http://views.grails.org/latest/#_the_jsontemplateengine
 *
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@CompileStatic
class JsonTools {

    static JsonGenerator generator

    static String toJson(Object object){
        JsonOutput.toJson(object)
    }

    static String render(Object object, Map arguments = [:]){
        if(!generator) initGenerator()
        generator.toJson(object)
    }

    static void initGenerator(){
        // exclude nulls by default
        generator = new JsonGenerator.Options()
            .excludeNulls()
            .build()
    }
}
