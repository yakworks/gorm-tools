package gorm.tools.databinding

import org.grails.datastore.gorm.GormEntity

/**
 * binds data from a map to a GormEntity. The map can of course be a JSONObject as is common when binding rest resources
 */
interface MapBinder {

    void bind(Object target, Map<String, Object> source, String bindMethod)

    void bind(Object target, Map<String, Object> source)

    void bindCreate(Object target, Map<String, Object> source)

    void bindUpdate(Object target, Map<String, Object> source)

}
