/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit.ast

import groovy.transform.CompileStatic

@SuppressWarnings(['ClassForName', 'ThrowRuntimeException'])
@CompileStatic
class FieldProps {
    // createdBy.field = "createdBy" // createdBy is default
    // createdBy.constraints = "nullable:true,display:false,editable:false,bindable:false"
    // createdDate.field = "createdDate"
    // createdDate.constraints = "nullable:true,display:false,editable:false,bindable:false"
    //
    // editedBy.field = "editedBy" // createdBy is default
    // editedBy.constraints = "nullable:true,display:false,editable:false,bindable:false"
    // editedDate.field = "editedDate"
    // editedDate.constraints = "nullable:true,display:false,editable:false,bindable:false"
    static final String CONFIG_KEY = "grails.plugin.audittrail"
    static final String DATE_CONS = "nullable:true, display:false, editable:false, bindable:false"
    static final String USER_CONS = "nullable:true, display:false, editable:false, bindable:false"

    static final String CREATED_DATE_KEY = "createdDate"
    static final String EDITED_DATE_KEY = "editedDate"
    static final String CREATED_BY_KEY = "createdBy"
    static final String EDITED_BY_KEY = "editedBy"


    String name
    Class type
    String constraints
    String mapping

    private static Object getConfigValue(Map config, String key, Object defaultValue) {
        //System.out.println(key + ":" + defaultValue.toString() + ":" + config.get(key))
        return (config?.containsKey(key)) ? config.get(key) : defaultValue
    }

    static FieldProps init(String defaultName, String defaultType, String defaultCons, String defaultMapping, Map configObj) {
        //if(configObj == null || configObj.isEmpty()) return null

        String baseKey = CONFIG_KEY + "." + defaultName

        Map map = (Map) getMap(configObj, baseKey)
        // if(map == null){
        //     return null
        // }

        FieldProps newField = new FieldProps()
        newField.name = (String) getConfigValue(map, "field", defaultName)

        String className = (String) getConfigValue(map, "type", defaultType)

        if (className == null || className == "") {
            className = defaultType
        }

        try {
            newField.type = Class.forName(className)
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class " + className + " could not be found for audittrail setting " + defaultName)
        }
        if (map?.containsKey("constraints")) {
            newField.constraints = (String) map.get("constraints")
        } else {
            newField.constraints = defaultCons
        }
        if (map?.containsKey("mapping")) {
            newField.mapping = (String) map.get("mapping")
        }
        // System.out.println("newField: " + newField)
        return newField
    }

    static Map<String, FieldProps> buildFieldMap(Map config) {
        Map<String, FieldProps> map = [:] as Map<String, FieldProps>
        map.put(CREATED_BY_KEY, FieldProps.init(CREATED_BY_KEY, "java.lang.Long", USER_CONS, null, config))
        map.put(EDITED_BY_KEY, FieldProps.init(EDITED_BY_KEY, "java.lang.Long", USER_CONS, null, config))


        map.put(EDITED_DATE_KEY, FieldProps.init(EDITED_DATE_KEY, "java.time.LocalDateTime", DATE_CONS, null, config))
        map.put(CREATED_DATE_KEY, FieldProps.init(CREATED_DATE_KEY, "java.time.LocalDateTime", DATE_CONS, null, config))
        return map
    }

    static Object getMap(Map configMap, String keypath) {
        String[] keys = keypath.split("\\.")
        Map map = configMap
        for (String key : keys) {
            Object val = map.get(key)
            // println "key: $key , val: $val"
            if (val != null) {
                //System.out.println("got a key for are " +key)
                if (val instanceof Map) {
                    map = (Map) map.get(key)
                } else {
                    return val
                }
            } else {
                return null
            }
        }
        return map
    }
}
