package gorm.tools.databinding

import grails.databinding.DataBinder

/**
 * binds data from a map to a GormEntity. The map can of course be a JSONObject as is common when binding rest resources
 */
interface MapBinder extends DataBinder{

    void bind(Map args, Object target, Map<String, Object> source, BindAction bindAction)

    void bind(Object target, Map<String, Object> source, BindAction bindAction)
}
