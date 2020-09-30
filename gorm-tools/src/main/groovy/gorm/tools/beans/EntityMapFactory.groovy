/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans

import java.lang.reflect.ParameterizedType

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.reflection.CachedMethod
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association

import gorm.tools.utils.GormMetaUtils

//import org.apache.commons.logging.*

/**
 * EntityMapFactory contains a set of helpers, which will create the EntityMap and Lists
 * from Gorm Domains
 */
@Slf4j
@CompileStatic
class EntityMapFactory {

    private EntityMapFactory() {
        throw new AssertionError()
    }

    /**
     * Wrap entity/object in EntityMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    static EntityMap createEntityMap(Object entity, List<String> includes) {
        EntityMapIncludes includesMap = buildIncludesMap(entity.class.name, includes)
        return new EntityMap(entity, includesMap)
    }

    /**
     * Wrap entity/object in EntityMap
     *
     * @param entity the entity to wrap in a map
     * @param includes the fields list to include. ex ['*', 'foo.bar', 'foo.id']
     * @return the EntityMap object
     */
    static EntityMapList createEntityMapList(List entityList, List<String> includes = []) {
        if(!entityList) return null
        //use first item to get the class
        Class entityClass = entityList[0].class.name
        EntityMapIncludes includesMap = buildIncludesMap(entityClass.name, includes)
        return new EntityMapList(entityList, includesMap)
    }

    /**
     * builds a EntityMapIncludes object from a sql select like list. Used in EntityMap and EntityMapList
     *
     * @param className the class name of the PersistentEntity
     * @param includes
     * @return the EntityMapIncludes object that can be passed to EntityMap
     */
    static EntityMapIncludes buildIncludesMap(String entityClassName, List<String> includes = []) {
        includes = includes ?: ['*'] as List<String> //default to * if nothing
        // if(!entityClass && entity) entityClass = entity.class
        PersistentEntity domain = GormMetaUtils.findPersistentEntity(entityClassName)
        //assert domain, "$entityClassName did not return a PersistentEntity"
        List<PersistentProperty> properties = domain ? GormMetaUtils.getPersistentProperties(domain) : []

        Set<String> rootProps = [] as Set<String>
        Map<String, Object> nestedProps = [:]

        for (String field : includes) {
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
                    String nestedClass = pp?.getAssociatedEntity()?.name
                    Map<String,Object> initMap = ['className': nestedClass, 'props': [] as Set]
                    nestedProps[nestedProp] = initMap
                }
                String nestedPropPath = field.substring(nestedIndex+1)
                def initMap = nestedProps[nestedProp] as Map<String,Object>
                (initMap['props'] as Set).add(nestedPropPath)
            }
        }
        def entIncludes = new EntityMapIncludes(entityClassName, rootProps)
        //Map<String, Object> propMap = [className: className, props: rootProps]
        // now cycle through the nested props and recursively call this
        Map<String, EntityMapIncludes> nestedMap = [:]
        for (entry in nestedProps.entrySet()) {
            def prop = entry.key as String
            Map initMap = entry.value as Map
            List incProps = initMap['props'] as List
            String assocClass = initMap['className'] as String
            EntityMapIncludes nestedItem
            // if no nestClassName then it wasn't a gorm association from above so try by getting value through meta reflection
            if(!assocClass) {
                Class entityClass = loadClass(entityClassName)
                MetaBeanProperty mbp = getMetaBeanProp(entityClass, prop)
                Class returnType = mbp.getter.returnType
                if(Collection.isAssignableFrom(returnType)){
                    String genClass = findGenericForCollection(entityClass, prop)
                    if(genClass) {
                        nestedItem = buildIncludesMap(genClass, incProps)
                    }
                } else {
                    nestedItem = buildIncludesMap(returnType.name, incProps)
                }
            } else {
                nestedItem = buildIncludesMap(assocClass, incProps)
            }
            if(!nestedMap[prop]) nestedMap[prop] = nestedItem
        }

        if(nestedMap) entIncludes.nestedIncludes = nestedMap

        return entIncludes
    }

    static MetaBeanProperty getMetaBeanProp(Class entityClass, String prop) {
        return entityClass.metaClass.properties.find{ it.name == prop} as MetaBeanProperty
    }

    static String findGenericForCollection(Class entityClass, String prop){
        MetaBeanProperty metaProp = getMetaBeanProp(entityClass, prop)
        CachedMethod gen = metaProp.getter as CachedMethod
        def genericReturnType = gen.cachedMethod.genericReturnType as ParameterizedType
        def actualTypeArguments = genericReturnType.actualTypeArguments
        actualTypeArguments ? actualTypeArguments[0].typeName : null
    }

    static Class loadClass(String clazz){
        def classLoader = Thread.currentThread().contextClassLoader
        classLoader.loadClass(clazz)
    }

}
