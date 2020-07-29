/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.json

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

import gorm.tools.beans.AppCtx
import gorm.tools.beans.BeanPathTools
import grails.plugin.json.builder.JsonOutput.JsonWritable
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.JsonViewWritableScript
import grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper
import grails.plugin.json.view.template.JsonViewTemplate

/**
 * A helper class that uses the logic in grails json view plugin to generate json without needing
 * a gson file and outside the scope of and HTTP request using the JsonViewTemplateEngine
 * see http://views.grails.org/latest/#_the_jsontemplateengine
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class Jsonify {
//
//    @Autowired
//    JsonViewTemplateEngine jsonViewTemplateEngine

    static private JsonViewTemplate cachedEmptyTemplate

    static JsonViewTemplate getViewTemplate() {
        if (!cachedEmptyTemplate) {
            //create an empty and emptyTemplate and cache. we only use it to get to the DefaultGrailsJsonViewHelper which has the beans we need setup
            cachedEmptyTemplate = (JsonViewTemplate)AppCtx.get("jsonTemplateEngine", JsonViewTemplateEngine).createTemplate('')
        }
        return cachedEmptyTemplate
    }

    static DefaultGrailsJsonViewHelper getViewHelper() {
        //make the script class so we can get the DefaultGrailsJsonViewHelper which has the
        JsonViewWritableScript jv = (JsonViewWritableScript) getViewTemplate().make()
        (DefaultGrailsJsonViewHelper) jv.getG()
    }

    /** see {@link grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper#render} */
    static JsonWritable renderWritable(Object object, Map arguments = Collections.emptyMap(),
                                   @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer = null) {

        getViewHelper().render(object, arguments, customizer)

    }

    /**
     * Useful for testing. Returns the result with the json object (map or collection) and the jsonText
     * see {@link grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper#render} for arguments
     *
     * @param object
     * @param arguments [includes, excludes, deep, associations, expand]
     *   see {@link grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper#render} and json-views for more details
     * @param customizer
     * @return
     */
    static JsonifyResult render(Object object, Map arguments = Collections.emptyMap(),
                                   @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate) Closure customizer = null ) {
        if (arguments.includes && object){
            List origIncludes  = arguments.includes as List<String>
            // println("origIncludes ${origIncludes}")
            String className = object instanceof List ? (object as List)[0].class.name : object.class.name
            List newIncludes = BeanPathTools.getIncludes(className, origIncludes)
            arguments.includes = newIncludes
        }
        JsonWritable w = renderWritable(object, arguments, customizer)
        return new JsonifyResult(writer: w)
    }

    @CompileStatic
    static class JsonifyResult {
        JsonWritable writer
        /**
         * The JSON result
         */
        Object json
        /**
         * The raw JSON text
         */
        String jsonText

        String getJsonText(){
            if(!jsonText) jsonText = writer.toString()
            jsonText
        }

        Object getJson(){
            if(!json) json = new JsonSlurper().parseText(getJsonText())
            json
        }

        @Override
        String toString() {
            getJsonText()
        }

    }
}
