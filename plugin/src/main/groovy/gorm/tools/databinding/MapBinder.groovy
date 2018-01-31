package gorm.tools.databinding

import grails.databinding.DataBinder
import groovy.transform.CompileStatic

/**
 * binds data from a map to a GormEntity. The map can of course be a JSONObject as is common when binding rest resources
 */
@CompileStatic
interface MapBinder extends DataBinder {

    /**
     * Binds data from a map on target object.
     *
     * @param args a Map of options
     * @param target The target object to bind
     * @param source The source map
     */
    void bind(Map args, Object target, Map<String, Object> source)

    /**
     * Binds data from a map on target object.
     *
     * @param target The target object to bind
     * @param source The source map
     */
    void bind(Object target, Map<String, Object> source)
}
