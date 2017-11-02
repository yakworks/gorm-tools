package gorm.tools.beans

import gorm.tools.GormMetaUtils
import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.util.GrailsClassUtils
import grails.util.GrailsNameUtils
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.lang.Validate
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.gorm.GormEntity

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
    //static Log log = LogFactory.getLog(getClass())
    static GrailsApplication grailsApplication = Holders.grailsApplication
    private static Map excludes = [hasMany: true, belongsTo: true, searchable: true, __timeStamp: true,
                                   constraints: true, version: true, metaClass: true]

    private BeanPathTools() {
        throw new AssertionError()
    }

    //@CompileDynamic
    static Object getNestedValue(domain, String field) {
//        String[] subProps = field.split("\\.")
//
//        int i = 0
//        Object lastProp
//        for (prop in subProps) {
//            if (i == 0) {
//                lastProp = domain."$prop"
//            } else {
//                lastProp = lastProp."$prop"
//            }
//            i += 1
//        }
//
//        return lastProp
        GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(domain, field)
    }

    @CompileDynamic
    //didn't find any usage of this method, so marked it as deprecated
    @Deprecated
    static Object getFieldValue(Object domain, String field) {
        Object bean = getNestedBean(domain, field)
        field = GrailsNameUtils.getShortName(field)
        return bean?."$field"
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
            if (!excludes.containsKey(field.name) && !field.name.contains("class\$") && !field.name.startsWith("__timeStamp")) {
                props.add(field.name)
            }
        }

        props.sort()

        return props
    }

    /**
     * Provides an ability to retrieve object's fields into a map.
     * It is possible to specify a list of fields which should be picked up.
     *
     * Note: propList = ['*'] represents all fields.
     *
     * @param source            a source object
     * @param propList          a list of properties to include to a map
     * @param useDelegatingBean
     * @return a map which is based on object properties
     */
    @CompileDynamic
    static Map buildMapFromPaths(Object source, List<String> propList, boolean useDelegatingBean = false) {
        if (useDelegatingBean) {
            Class delegatingBean = GrailsClassUtils.getStaticFieldValue(source.getClass(), "delegatingBean")
            if (!delegatingBean && Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, source.getClass())) {
                delegatingBean = DaoDelegatingBean
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
     * @param source        a source object
     * @param propertyPath  a property name, e.g. 'someField', 'someField.nestedField', '*' (for all properties)
     * @param currentMap    a destination map
     * @return a map which contains an object's property (properties)
     */
    @SuppressWarnings(['ReturnsNullInsteadOfEmptyCollection', 'CatchException'])
    @CompileDynamic
    static Map propsToMap(Object source, String propertyPath, Map currentMap) {
        if (source == null) return null
        Integer nestedIndex = propertyPath.indexOf('.')
        //no index then its just a property or its the *
        if (nestedIndex == -1) {
            if (propertyPath == '*') {
                if (log.debugEnabled) log.debug("source:$source propertyPath:$propertyPath currentMap:$currentMap")

                //just get the persistentProperties
                Object object = (source instanceof DelegatingBean) ? ((DelegatingBean)source).target : source

                if (object instanceof GormEntity) {
                    //FIXME this makes it hard to test, fix it so its easier to mock
                    GrailsDomainClass domainClass = GormMetaUtils.getDomainClass(object)
                    //FIXME why only persistentProperties, seems we should allow any of them no?
                    GrailsDomainClassProperty[] pprops = domainClass.persistentProperties
                    //filter out the associations. need to explicitly add those to be included
                    pprops = pprops.findAll { p -> !p.isAssociation() }
                    //force the the id to be included
                    String id = domainClass.getIdentifier().name
                    currentMap[id] = source?."$id"
                    //spin through and add them to the map
                    pprops.each { property ->
                        currentMap[property.name] = source?."$property.name"
                    }
                } else {
                    Closure notConvert = { it instanceof Map || it instanceof Collection ||
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
            } else if (source?.hasProperty(propertyPath)) {
                currentMap[propertyPath] = source."$propertyPath"
            }
        } else {
            // We have at least one sub-key, so extract the first element
            // of the nested key as the prefix. In other words, if we have
            // 'nestedKey' == "a.b.c", the prefix is "a".
            String nestedPrefix = propertyPath.substring(0, nestedIndex)

            if (source?.hasProperty(nestedPrefix)) {
                if (!currentMap.containsKey(nestedPrefix) || !(currentMap[nestedPrefix] instanceof Map)) {
                    currentMap[nestedPrefix] = [:]
                }
                Object nestedObj = source."$nestedPrefix"
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
    static GrailsParameterMap getGrailsParameterMap(Map p, HttpServletRequest request){
        GrailsParameterMap gpm = new GrailsParameterMap(p, request)
        gpm.updateNestedKeys(p)
        return gpm
    }

}