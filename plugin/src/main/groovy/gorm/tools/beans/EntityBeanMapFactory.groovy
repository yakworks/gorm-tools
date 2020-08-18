/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import javax.servlet.http.HttpServletRequest

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import gorm.tools.GormMetaUtils
import grails.util.GrailsClassUtils
import grails.web.servlet.mvc.GrailsParameterMap

//import org.apache.commons.logging.*

/**
 * EntityBeanMapFactory contains a set of helpers, which will create the EntityBeanMap and Lists
 * from Gorm Domains
 */
@Slf4j
@CompileStatic
class EntityBeanMapFactory {

    private EntityBeanMapFactory() {
        throw new AssertionError()
    }

    static EntityBeanMap createEntityBeanMap(Object entity, List<String> fields) {
        Map<String, Object> includesMap = getIncludesForBeanMap(entity.class.name, fields)
        return new EntityBeanMap(entity, includesMap)
    }

    static Map<String, Object> getIncludesForBeanMap(String className, List<String> fields) {
        PersistentEntity domain = GormMetaUtils.findPersistentEntity(className)
        List<PersistentProperty> properties = GormMetaUtils.getPersistentProperties(domain)
        Set<String> rootProps = [] as Set<String>
        Map<String, Object> nestedProps = [:]
        for (String field : fields) {
            Integer nestedIndex = field.indexOf('.')
            //no index then its just a property or its the *
            if (nestedIndex == -1) {
                if (field == '*') {
                    List<String> props = properties.findAll { !(it instanceof Association) }*.name
                    rootProps.addAll(props)
                }
                else { //normal prop
                    // todo should add check for transient
                    rootProps.add(field)
                }
            }
            else { // has a . and is a nested prop
                String nestedProp = field.substring(0, nestedIndex)
                rootProps.add(nestedProp)

                if(!nestedProps[nestedProp]) {
                    Association pp = properties.find { it.name == nestedProp } as Association
                    String nestedClass = pp?.getAssociatedEntity()?.getName()
                    Map<String,Object> initMap = ['className': nestedClass, 'props': [] as Set]
                    nestedProps[nestedProp] = initMap
                }
                String nestedPropPath = field.substring(nestedIndex+1)
                (nestedProps[nestedProp]['props'] as Set).add(nestedPropPath)
            }
        }
        Map<String, Object> propMap = [className: className, props: rootProps]
        // now cycle through the nested props and recursively call this
        Map<String, Object> nestedMap = [:]
        for (entry in nestedProps.entrySet()) {
            def props = entry.value as Map
            Map<String, Object> nestedItem = getIncludesForBeanMap(props['className'] as String, props['props'] as List)
            if(!nestedMap[entry.key]) nestedMap[entry.key] = nestedItem
        }
        if(nestedMap) propMap.nested = nestedMap

        return propMap
    }
}
