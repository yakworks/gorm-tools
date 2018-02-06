package gorm.tools.json

import gorm.tools.beans.AppCtx
import grails.plugin.json.builder.JsonOutput.JsonWritable
import grails.plugin.json.builder.StreamingJsonBuilder
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.JsonViewWritableScript
import grails.plugin.json.view.api.internal.DefaultGrailsJsonViewHelper
import grails.plugin.json.view.template.JsonViewTemplate
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic

/**
 * A helper class that uses the logic in grails json view plugin to generate json without needing
 * a gson file and outside the scope of and HTTP request using the JsonViewTemplateEngine
 * see http://views.grails.org/latest/#_the_jsontemplateengine
 */
@CompileStatic
class Jsonify {
//
//    @Autowired
//    JsonViewTemplateEngine jsonViewTemplateEngine

    static private JsonViewTemplate cachedEmptyTemplate

    static JsonViewTemplate getViewTemplate() {
        if (!cachedEmptyTemplate) {
            //create an empty and emptyTemplate and cache. we only use it to get to the DefaultGrailsJsonViewHelper
            cachedEmptyTemplate = (JsonViewTemplate)AppCtx.get("jsonTemplateEngine", JsonViewTemplateEngine).createTemplate('')
        }
        return cachedEmptyTemplate
    }

    static DefaultGrailsJsonViewHelper getViewHelper() {
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

        JsonWritable writer = renderWritable(object, arguments, customizer)
        return new JsonifyResult(writer: writer)
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
