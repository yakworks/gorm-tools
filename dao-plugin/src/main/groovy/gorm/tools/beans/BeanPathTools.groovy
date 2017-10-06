package gorm.tools.beans

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
import org.apache.juli.logging.Log
import org.apache.juli.logging.LogFactory
import org.grails.core.artefact.DomainClassArtefactHandler
import org.hibernate.Hibernate
import org.hibernate.UnresolvableObjectException
import org.hibernate.ObjectNotFoundException

import javax.servlet.http.HttpServletRequest

//import org.apache.commons.logging.*

//XXX add tests for this
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
    public static Object getNestedValue(domain, String field) {
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
        GrailsClassUtils.getPropertyOrStaticPropertyOrFieldValue(domain,field)
    }

    @CompileDynamic
    //XXX Is this used? whats it for? how is it different than getNestedValue?
    public static Object getFieldValue(Object domain, String field) {
        Object bean = getNestedBean(domain, field)
        field = GrailsNameUtils.getShortName(field)
        return bean?."$field"
    }
    /**
     * returns the depest nested bean
     * */
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
    public static List getFields(Object domain) {
        List props = []

        domain?.class?.properties?.declaredFields.each { field ->
            if (!excludes.containsKey(field.name) && !field.name.contains("class\$") && !field.name.startsWith("__timeStamp")) {
                props.add(field.name)
            }
        }

        props.sort()

        return props
    }

    //XXX add test for this
    @CompileDynamic
    static Map buildMapFromPaths(Object obj, List propList, boolean useDelegatingBean = false) {
        if (useDelegatingBean) {
            Class delegatingBean = GrailsClassUtils.getStaticFieldValue(obj.getClass(), "delegatingBean")
            if (delegatingBean == null && Holders.grailsApplication.isArtefactOfType(DomainClassArtefactHandler.TYPE, obj.getClass())) {
                delegatingBean = DaoDelegatingBean
            }
            if (delegatingBean != null) obj = delegatingBean.newInstance(obj)
        }
        //FIXME we should look into do something like LazyMetaPropertyMap in grails-core that wraps the object and delegates
        //the map key lookups to the objects
        Map rowMap = [:]
        propList.each { key ->
            propsToMap(obj, key, rowMap)
        }
        if (log.debugEnabled) log.debug(rowMap.toMapString())
        return rowMap

    }

    @CompileDynamic
    static Map propsToMap(Object obj, String propertyPath, Map currentMap) {
        if (obj == null) return
        final int nestedIndex = propertyPath.indexOf('.')
        //no idex then its just a property or its the *
        if (nestedIndex == -1) {
            if (propertyPath == '*') {
                if (log.debugEnabled) log.debug("obj:$obj propertyPath:$propertyPath currentMap:$currentMap")
                //just get the persistentProperties
                Object domain = (obj instanceof DelegatingBean) ? ((DelegatingBean)obj).target : obj
                //FIXME this makes it really hard to test, fix it so its easier to mock
                GrailsDomainClass domainClass = (GrailsDomainClass) grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, Hibernate.getClass(domain).name)
                //GrailsDomainClass domainClass = domain.domainClass
                if (domainClass == null) {
                    throw new RuntimeException("${obj.getClass().name} is not a domain class")
                }
                GrailsDomainClassProperty[] pprops = domainClass.persistentProperties
                //filter out the associations. need to explicitely add those to be included
                pprops = pprops.findAll { p -> !p.isAssociation() }
                //force the the id to be included
                String id = domainClass.getIdentifier().name
                currentMap[id] = obj?."$id"
                //spin through and add them to the map
                pprops.each { Map it ->
                    currentMap[it.name] = obj?."$it.name"
                }
            } else {
                try {
                    currentMap[propertyPath] = obj?."$propertyPath"
                } catch (UnresolvableObjectException e) {
                    log.error("Cannot set value for $propertyPath ($e.entityName, id $e.identifier). $e.message")
                } catch (Exception e) {
                    log.error("Cannot set value for $propertyPath from $obj", e)
                }
            }

            return null
        } else {
            // We have at least one sub-key, so extract the first element
            // of the nested key as the prfix. In other words, if we have
            // 'nestedKey' == "a.b.c", the prefix is "a".
            String nestedPrefix = propertyPath.substring(0, nestedIndex)
            if (!currentMap.containsKey(nestedPrefix)) {
                currentMap[nestedPrefix] = [:]
            }
            if (!(currentMap[nestedPrefix] instanceof Map)) {
                currentMap[nestedPrefix] = [:]
            }

            Object nestedObj = null
            try {
                nestedObj = obj."$nestedPrefix"
            } catch (UnresolvableObjectException e) {
                log.error("Cannot set value for $nestedPrefix ($e.entityName, id $e.identifier). $e.message")
            } catch (Exception e) {
                log.error("Cannot set value for $nestedPrefix from $obj", e)
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
            return null
        }

    }

    /**
     * takes a request and an optional map.
     * call the MapFlattener and returns a GrailsParameterMap to be used for binding
     * example: [xxx:[yyy:123]] will turn into a GrailsParameterMap with ["xxx.yyy":123]
     */
    //XXX add test for these
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