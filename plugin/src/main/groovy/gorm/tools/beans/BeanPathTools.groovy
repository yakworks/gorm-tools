/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package gorm.tools.beans

import gorm.tools.GormMetaUtils
import grails.util.GrailsClassUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import javax.servlet.http.HttpServletRequest

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

    private static final List<String> EXCLUDES = ['hasMany', 'belongsTo', 'searchable', '__timeStamp',
                                                  'constraints', 'version', 'metaClass']

    private BeanPathTools() {
        throw new AssertionError()
    }

    //@CompileDynamic
    static Object getFieldValue(domain, String field) {
        GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(domain, field)
    }

    /**
     * Returns the deepest nested bean
     */
    @CompileDynamic
    static getNestedBean(Object bean, String path) {
        int i = path.lastIndexOf(".")
        if (i > -1) {
            path = path.substring(0, i)
            path.split('\\.').each { bean = bean?."$it" }
        }
        return bean
    }

    @CompileDynamic
    static List getFields(Object domain) {
        List props = []

        domain?.class?.properties?.declaredFields.each { field ->
            if (!EXCLUDES.contains(field.name) && !field.name.contains("class\$") && !field.name.startsWith("__timeStamp")) {
                props.add(field.name)
            }
        }

        props.sort()

        return props
    }

    /**
     * Provides an ability to retrieve object's fields into a map.
     * It is possible to specify a query of fields which should be picked up.
     *
     * Note: propList = ['*'] represents all fields.
     *
     * @param source a source object
     * @param propList a query of properties to include to a map
     * @param useDelegatingBean
     * @return a map which is based on object properties
     */
    @CompileDynamic
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
     *
     * @param source a source object
     * @param propertyPath a property name, e.g. 'someField', 'someField.nestedField', '*' (for all properties)
     * @param currentMap a destination map
     * @return a map which contains an object's property (properties)
     */
    @SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection', 'CyclomaticComplexity'])
    //FIXME refactor so CyclomaticComplexity doesn't fire in codenarc
    @CompileDynamic
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
                    pprops = pprops.findAll { p -> !(p instanceof Association && p.associatedEntity) }
                    //force the the id to be included
                    String id = domainClass.identity.name
                    currentMap[id] = source?."$id"
                    //spin through and add them to the map
                    pprops.each { property ->
                        currentMap[property.name] = source?."$property.name"
                    }
                } else {
                    Closure notConvert = {
                        it instanceof Map || it instanceof Collection ||
                                it instanceof Number || it?.class in [String, Boolean, Character]
                    }
                    Map props = object.properties.findAll { it.key != 'class' }
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
                    currentMap[propertyPath] = source?."$propertyPath"
                } catch (Exception e) {
                }
            }
        } else {
            // We have at least one sub-key, so extract the first element
            // of the nested key as the prefix. In other words, if we have
            // 'nestedKey' == "a.b.c", the prefix is "a".
            String nestedPrefix = propertyPath.substring(0, nestedIndex)


            if (!currentMap.containsKey(nestedPrefix) || !(currentMap[nestedPrefix] instanceof Map)) {
                currentMap[nestedPrefix] = [:]
            }
            Object nestedObj

            try {
                nestedObj = source."$nestedPrefix"
            } catch (Exception e) {
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
     * takes a request and an optional map.
     * call the MapFlattener and returns a GrailsParameterMap to be used for binding
     * example: [xxx:[yyy:123]] will turn into a GrailsParameterMap with ["xxx.yyy":123]
     */
    static GrailsParameterMap flattenMap(HttpServletRequest request, Map jsonMap = null) {
        Map p = new MapFlattener().flatten(jsonMap ?: (Map) request.JSON)
        return getGrailsParameterMap(p, request)
    }

    @CompileDynamic
    static List<String> getIncludes(String className, List<String> fields) {
        List<PersistentProperty> properties = GormMetaUtils.getPersistentProperties(className)
        List<String> result = []
        fields.each { String field ->
            if (field == "*") {
                result.addAll(properties.findAll { !(it instanceof Association) }*.name)
            } else if (field.endsWith(".*")) {
                String[] path = field.split("[.]")
                String nestedClass = properties.find { it.name == path[0] }?.getAssociatedEntity()?.getName()
                if (nestedClass)
                    result = result + getIncludes(nestedClass, [path.tail().join(".")]).collect { "${path[0]}.${it}" }
                if (path.size() > 1) result = result + [path[0]]
                result = result*.toString()
            } else {
                result << field // TODO: should we check that field really exists?
            }
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
