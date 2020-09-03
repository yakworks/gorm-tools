package gorm.tools.compiler.stamp;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

public class FieldProps {
    public static final String CONFIG_KEY = "grails.plugin.audittrail";
    private static final String DATE_CONS = "nullable:false, display:false, editable:false, bindable:false";
    private static final String USER_CONS = "nullable:false, display:false, editable:false, bindable:false";

    public static final String CREATED_DATE_KEY = "createdDate";
    public static final String EDITED_DATE_KEY = "editedDate";
    public static final String CREATED_BY_KEY = "createdBy";
    public static final String EDITED_BY_KEY = "editedBy";


    public String name;
    public Class  type;
    public String constraints;
    public String mapping;

    private static Object getConfigValue(Map config, String key, Object defaultValue) {
        //System.out.println(key + ":" + defaultValue.toString() + ":" + config.get(key));
        if(config.containsKey(key)) return config.get(key);
        else return defaultValue;
    }

    public static FieldProps init(String defaultName, String defaultType, String defaultCons, String defaultMapping, Map configObj) {
        System.out.println("ConfigObject : " + configObj);
        //if(configObj == null || configObj.isEmpty()) return null;

        String baseKey = CONFIG_KEY + "." + defaultName;

        Map map = (Map) getMap(configObj, baseKey);
        // if(map == null){
        //     return null;
        // }

        FieldProps newField = new FieldProps();
        newField.name = (String)getConfigValue(map, "field", defaultName);

        String className = (String)getConfigValue(map,  "type", defaultType);

        if(className == null || className == "") {
            className = defaultType;
        }

        try {
            newField.type = Class.forName(className);
        }catch (ClassNotFoundException e) {
            throw new RuntimeException("Class " + className + " could not be found for audittrail setting " + defaultName);
        }
        if(!map.containsKey("constraints") ){
             newField.constraints = defaultCons;
        }else{
             newField.constraints = (String)map.get( "constraints");
        }
        if(map.containsKey("mapping") ){
            newField.mapping = (String)map.get("mapping");
        }
        System.out.println("newField: " + newField);
        return newField;
    }

    public static Map<String, FieldProps> buildFieldMap(Map config){
        Map<String, FieldProps> map = new HashMap<String, FieldProps>();
        map.put(CREATED_BY_KEY,FieldProps.init(CREATED_BY_KEY,"java.lang.Long",USER_CONS,null,config));
        map.put(EDITED_BY_KEY,FieldProps.init(EDITED_BY_KEY,"java.lang.Long",USER_CONS,null,config));


        map.put(EDITED_DATE_KEY,FieldProps.init(EDITED_DATE_KEY,"java.util.Date",DATE_CONS,null,config));
        map.put(CREATED_DATE_KEY,FieldProps.init(CREATED_DATE_KEY,"java.util.Date",DATE_CONS,null,config));
        return map;
    }

    static public Object getMap(Map configMap, String keypath) {
        String keys[] = keypath.split("\\.");
        Map map = configMap;
        for(String key : keys){
            Object val = map.get(key);
            if(val !=null) {
                //System.out.println("got a key for are " +key);
                if(val instanceof Map){
                    map = (Map)map.get(key);
                } else{
                    return val;
                }
            }
            else {
                return Collections.emptyMap();
            }
        }
        return map;
    }
}
