/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import gorm.tools.utils.GormMetaUtils
import grails.util.GrailsClassUtils
import grails.web.servlet.mvc.GrailsParameterMap
import yakworks.commons.lang.Validate
import yakworks.commons.map.MapFlattener

//import org.apache.commons.logging.*

/**
 * BeanPathTools contains a set of static helpers, which provides a convenient way
 * for manipulating with object's properties.
 *
 * For example, it allows to retrieve object's properties using filters and place them in a map.
 */
@Slf4j
@CompileStatic
class BeanPathTools {

//    private static final List<String> EXCLUDES = ['hasMany', 'belongsTo', 'searchable', '__timeStamp',
//                                                  'constraints', 'version', 'metaClass']

    private BeanPathTools() {
        throw new AssertionError()
    }

    static Object getFieldValue(Object domain, String field) {
        GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(domain, field)
    }

    /**
     * Return the value of the (possibly nested) property of the specified name, for the specified source object
     *
     * Example getPropertyValue(source, "x.y.z")
     *
     * @param source - The source object
     * @param property - the property
     * @return value of the specified property or null if any of the intermediate objects are null
     */
    static Object getPropertyValue(Object source, String property) {
        Validate.notNull(source, '[source]')
        Validate.notEmpty(property, '[property]')

        Object result = property.tokenize('.').inject(source) { Object obj, String prop ->
            Object value = null
            if (obj != null && obj.hasProperty(prop)) value = obj[prop]
            return value
        }

        return result
    }

    /**
     * Returns the deepest nested bean
     */
    static getNestedBean(Object bean, String path) {
        int i = path.lastIndexOf(".")
        if (i > -1) {
            path = path.substring(0, i)
            path.split('\\.').each { String it -> bean = bean[it] }
        }
        return bean
    }

//    @CompileDynamic
//    static List getFields(Object domain) {
//        List props = []
//
//        domain?.class?.properties?.declaredFields.each { field ->
//            if (!EXCLUDES.contains(field.name) && !field.name.contains("class\$") && !field.name.startsWith("__timeStamp")) {
//                props.add(field.name)
//            }
//        }
//        props.sort()
//
//        return props
//    }

    /**
     * Provides an ability to retrieve object's fields into a map.
     * It is possible to specify a query of fields which should be picked up.
     *
     * Note: propList = ['*'] represents all fields.
     *
     * @param source a source object
     * @param propList a query of properties to include to a map
     * @param useDelegatingBean if it has a delegating bean
     * @return a map which is based on object properties
     */
    @Deprecated //Use EntityMap instead now
    static Map buildMapFromPaths(Object source, List<String> propList, boolean useDelegatingBean = false) {
        if (useDelegatingBean) {
            Class delegatingBean = GrailsClassUtils.getStaticFieldValue(source.getClass(), "delegatingBean")
            if (!delegatingBean && source instanceof GormEntity) {
                delegatingBean = RepoDelegatingBean
            }
            if (delegatingBean != null) source = delegatingBean.newInstance(source)
        }
        //FIXME we should look into do something like LazyMetaPropertyMap in grails-core that wraps the object and delegates
        //the map key lookups to the objects
        Map rowMap = [:]
        propList.each { String key ->
            propsToMap(source, key, rowMap)
        }
        if (log.debugEnabled) log.debug(rowMap.toMapString())
        return rowMap
    }

    /**
     * Provides an ability to retrieve a property of a source object and add it to a map.
     * In case if 'propertyPath' is set to '*', it will extract all properties from a source object.
     * It is possible to extract a nested property by using the '.' symbol, e.g. 'property.nestedProperty'
     *
     * For example:
     *   object = new Test(id: 1, value: 10, nested: new Test1(foo: '1'))
     *
     *   results of propsToMap for the object will be:
     *   propsToMap(object, '*', map)                // [id: 1, value: 10, nested: [foo: 1]]
     *   propsToMap(object, 'id', map)               // [id: 1]
     *   propsToMap(object, 'nested.foo', map)       // [nested: [foo: 1]]
     *
     * @param source a source object
     * @param propertyPath a property name, e.g. 'someField', 'someField.nestedField', '*' (for all properties)
     * @param currentMap a destination map
     * @return a map which contains an object's property (properties)
     */
    @SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection', 'CyclomaticComplexity', 'EmptyCatchBlock'])
    //FIXME refactor so CyclomaticComplexity doesn't fire in codenarc
    static Map propsToMap(Object source, String propertyPath, Map currentMap) {
        if (source == null) return null
        Integer nestedIndex = propertyPath.indexOf('.')
        //no index then its just a property or its the *
        if (nestedIndex == -1) {
            if (propertyPath == '*') {
                log.debug("source:$source propertyPath:$propertyPath currentMap:$currentMap")

                //just get the persistentProperties
                Object object = (source instanceof DelegatingBean) ? ((DelegatingBean) source).target : source

                if (object instanceof GormEntity) {
                    PersistentEntity domainClass = GormMetaUtils.getPersistentEntity(object)
                    PersistentProperty[] pprops = domainClass.persistentProperties

                    //filter out the associations. need to explicitly add those to be included
                    pprops = pprops.findAll { PersistentProperty p -> !(p instanceof Association && p.associatedEntity) }
                    //force the the id to be included
                    String id = domainClass.identity.name
                    currentMap[id] = source[id]
                    //spin through and add them to the map
                    pprops.each { PersistentProperty property ->
                        try {
                            // currentMap[property.name] = source[property.name]
                            def val = convertValue(source, property.name)
                            currentMap[property.name] = val
                        } catch(e){
                            if (log.debugEnabled) log.debug("${source.class.name} with id ${source[id]} is not in db for property ${property.name}")

                        }
                    }
                } else {
                    Closure notConvert = {
                        it instanceof Map || it instanceof Collection ||
                        it instanceof Number || it?.class in [String, Boolean, Character]
                    }
                    Map props = object.properties.findAll { it.key != 'class' } as Map<String,?>
                    props.each { String name, Object value ->
                        if (!value || notConvert(value)) {
                            currentMap[name] = value
                        } else {
                            currentMap[name] = [:]
                            propsToMap(value, '*', (Map) currentMap[name])
                        }
                    }
                }

                // I think it would be enough to check if a property exists.
                // So it's the same as catching MissingPropertyException and do nothing if there is no property
            } else {
                try {
                    currentMap[propertyPath] = convertValue(source, propertyPath)
                } catch (Exception e) {
                    //TODO handle missing property exception so it can be reported
                }
            }
        } else {
            // We have at least one sub-key, so extract the first element
            // of the nested key as the prefix. In other words, if we have
            // 'nestedKey' == "a.b.c", the prefix is "a".
            String nestedPrefix = propertyPath.substring(0, nestedIndex)
            boolean newKey = false //check if this key is encountered first time, used to remove the key from map if property not found.
            if (!currentMap.containsKey(nestedPrefix) || !(currentMap[nestedPrefix] instanceof Map)) {
                currentMap[nestedPrefix] = [:]
                newKey = true
            }
            Object nestedObj

            try {
                nestedObj = source[nestedPrefix]
            } catch (Exception e) {
                //TODO handle missing property exception
                //remove the entry as the key doesnt exist and its going to be empty
                if(newKey) currentMap.remove(nestedPrefix)
            }

            String remainderOfKey = propertyPath.substring(nestedIndex + 1, propertyPath.length())
            //recursive call
            if (nestedObj instanceof Collection) {
                List l = []
                nestedObj.each { nestedObjItem ->
                    Map justForItem = [:]
                    propsToMap(nestedObjItem, remainderOfKey, justForItem)
                    l << justForItem
                }
                currentMap[nestedPrefix] = l
            } else {
                propsToMap(nestedObj, remainderOfKey, (Map) currentMap[nestedPrefix])
            }

        }

        return currentMap
    }

    /**
     * Converts value for propsToMap
     *
     * @param source the source object
     * @param key the property for the source object
     * @return the value to use
     */
    static Object convertValue(Object source, String propertyKey){
        Object val = source[propertyKey]
        // convert Enums to string
        if( val && val.class.isEnum()) {
            val = (val as Enum).name()
        } else if(val instanceof GormEntity) {
            // if it reached here then just generate the default id
            PersistentEntity domainClass = GormMetaUtils.getPersistentEntity(val)
            String id = domainClass.identity.name
            Map idMap = [id: val[id]]
            val = idMap
        }
        return val
    }

    /**
     * takes a request and an optional map.
     * call the MapFlattener and returns a GrailsParameterMap to be used for binding
     * example: [xxx:[yyy:123]] will turn into a GrailsParameterMap with ["xxx.yyy":123]
     */
    static GrailsParameterMap flattenMap(HttpServletRequest request, Map jsonMap = null) {
        Map p = new MapFlattener().flatten(jsonMap ?: (Map) request.JSON)
        return getGrailsParameterMap(p, request)
    }

    static List<String> getIncludes(String className, List<String> fields) {
        List<PersistentProperty> properties = GormMetaUtils.getPersistentProperties(className)
        List<String> result = []
        fields.each { String field ->
            Integer nestedIndex = field.indexOf('.')
            //no index then its just a property or its the *
            if (nestedIndex == -1) {
                if (field == '*') {
                    List<String> props = properties.findAll { !(it instanceof Association) }*.name
                    result.addAll(props)
                }
                else { //normal prop
                    // todo should add check for transient
                    result << field
                }
            }
            else { // nested field
                if (field.endsWith(".*")) {
                    String[] path = field.split("[.]")
                    Association pp = properties.find { it.name == path[0] } as Association
                    String nestedClass = pp.getAssociatedEntity()?.getName()
                    if (nestedClass) {
                        List<String> nprops = getIncludes(nestedClass, [path.tail().join(".")])
                        result.addAll( nprops.collect { "${path[0]}.${it}".toString() })
                    }
                    //if (path.size() > 1) result = result + [path[0]]
                    result = result*.toString() //makes sure they are all strings?
                } else {
                    result << field // TODO: should we check that field really exists?
                }
            }
            // if (field == "*") {
            //     List<String> props = properties.findAll { !(it instanceof Association) }*.name
            //     result.addAll(props)
            // } else if (field.endsWith(".*")) {
            //     String[] path = field.split("[.]")
            //     Association pp = properties.find { it.name == path[0] } as Association
            //     String nestedClass = pp.getAssociatedEntity()?.getName()
            //     if (nestedClass) {
            //         List<String> nprops = getIncludes(nestedClass, [path.tail().join(".")])
            //         result.addAll( nprops.collect { "${path[0]}.${it}".toString() })
            //     }
            //     //if (path.size() > 1) result = result + [path[0]]
            //     result = result*.toString() //makes sure they are all strings?
            // } else {
            //     result << field // TODO: should we check that field really exists?
            // }
        }
        result.unique()

    }


    @CompileDynamic
    static GrailsParameterMap getGrailsParameterMap(Map p, HttpServletRequest request) {
        GrailsParameterMap gpm = new GrailsParameterMap(p, request)
        gpm.updateNestedKeys(p)
        return gpm
    }

}
