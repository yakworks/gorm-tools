package gorm.tools.databinding

import org.grails.datastore.gorm.GormEntity

/**
 * binds data from a map to a GormEntity. The map can of course be a JSONObject as is common when binding rest resources
 */
interface MapDataBinder {

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source, String bindMethod)

    public <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source)

}
