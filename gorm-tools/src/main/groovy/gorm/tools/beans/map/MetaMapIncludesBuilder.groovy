/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.beans.map

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.ToMany

import gorm.tools.api.IncludesConfig
import gorm.tools.utils.GormMetaUtils
import grails.gorm.validation.ConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.PropertyTools

/**
 * Builder to create MetaMapIncludes from a sql select like list against an entity
 */
@Slf4j
@CompileStatic
@SuppressWarnings('InvertedIfElse')
class MetaMapIncludesBuilder {
    /**
     * Holds the list of fields that have display:false for a class, meaning they should not be exported
     */
    static final Map<String, Set<String>> BLACKLIST = new ConcurrentHashMap<String, Set<String>>()

    List<String> includes
    List<String> excludes = []
    String entityClassName
    Class entityClass
    PersistentEntity persistentEntity
    List<PersistentProperty> properties = []
    MetaMapIncludes metaMapIncludes

    MetaMapIncludesBuilder(String entityClassName, List<String> includes){
        this.entityClassName = entityClassName
        this.includes = includes ?: ['*'] as List<String>
        init()
    }

    MetaMapIncludesBuilder excludes(List<String> val) {
        if(val != null) this.excludes = val
        return this
    }

    void init(){
        this.persistentEntity = GormMetaUtils.findPersistentEntity(entityClassName)

        if(this.persistentEntity){
            //in case the short name was passed in make sure its full class name
            entityClassName = persistentEntity.javaClass.name
            properties = GormMetaUtils.getPersistentProperties(persistentEntity)
        }
        this.entityClass = ClassUtils.loadClass(entityClassName)
        this.metaMapIncludes = new MetaMapIncludes(entityClassName)
    }

    static MetaMapIncludes build(Class entityClass, List<String> includes){
        build(entityClass.name, includes, [])
    }

    // static MetaMapIncludes build(String entityClassName, List<String> includes){
    //     new MetaMapIncludesBuilder(entityClassName, includes).build()
    // }

    static MetaMapIncludes build(String entityClassName, List<String> includes, List<String> excludes = []){
        def mmib = new MetaMapIncludesBuilder(entityClassName, includes)
        if(excludes) mmib.excludes(excludes)
        return mmib.build()
    }

    /**
     * builds a EntityMapIncludes object from a sql select like list. Used in EntityMap and EntityMapList
     *
     * @param className the class name of the PersistentEntity
     * @param includes the includes list in our custom dot notation
     * @return the EntityMapIncludes object that can be passed to EntityMap
     */
    MetaMapIncludes build() {

        Set<String> rootProps = [] as Set<String>
        Map<String, Object> nestedProps = [:]

        for (String field : includes) {
            field = field.trim()
            Integer nestedIndex = field.indexOf('.')
            //if its has a dot then its an association, so grab the first part
            // for example if this is foo.bar.baz then this sets nestedPropName = 'foo'
            String nestedPropName = nestedIndex > -1 ? field.substring(0, nestedIndex) : null

            // if it is a nestedPropName then if it does NOT exist, continue
            if(nestedPropName && !propertyExists(nestedPropName)){
                continue
            }

            //no dot then its just a property or its the * or $stamp
            if (!nestedPropName) {
                if (field == '*') {
                    // * only works if it has properties
                    if(properties){
                        List<String> props = properties.findAll {
                            !(it instanceof ToMany)
                        }*.name
                        metaMapIncludes.fields.addAll(props)
                    }
                }
                //if it start with a $ then use it as includesKey
                else if (field.startsWith('$')) {
                    String incKey = field.replace('$', '')
                    Map incsMap = IncludesConfig.bean().getIncludes(entityClass)
                    if(incsMap){
                        List props = ( incsMap[incKey] ?: ['id'] ) as List<String>
                        def toMerge = MetaMapIncludesBuilder.build(entityClass.name, props)
                        metaMapIncludes.merge(toMerge)
                    }
                }
                //just a normal prop but make sure it exists
                else {
                    if(propertyExists(field)){
                        metaMapIncludes.fields.add(field)
                    }
                    // TODO should add check for transient?
                }
            } else {
                //we are sure its exists at this point as we alread checked above
                metaMapIncludes.fields.add(nestedPropName)

                //set it up if it has not been yet
                if (!nestedProps[nestedPropName]) {
                    PersistentProperty pp = properties.find { it.name == nestedPropName }
                    String nestedClass
                    // check if its association
                    if (pp instanceof Association) {
                        nestedClass = (pp as Association)?.getAssociatedEntity()?.name
                    } else {
                        nestedClass = pp?.type?.name
                    }
                    Map<String, Object> initMap = ['className': nestedClass, 'props': [] as Set]
                    nestedProps[nestedPropName] = initMap
                }
                //if prop is foo.bar.baz then this get the bar.baz part
                String propPath = field.substring(nestedIndex + 1)
                //if its * on nested then assume that means stamp
                // if(propPath == '*') propPath = '$stamp'
                // if its a $* then thats a hard override that we want *
                if(propPath == '$*') propPath = '$stamp'

                def initMap = nestedProps[nestedPropName] as Map<String, Object>
                (initMap['props'] as Set).add(propPath)
            }
        }
        //create the includes class for what we have now along with the the blacklist
        Set blacklist = getBlacklist(persistentEntity) + (this.excludes as Set)

        //only if it has rootProps
        if (metaMapIncludes.fields) {
            if(blacklist) metaMapIncludes.addBlacklist(blacklist)
            //if it has nestedProps then go recursive
            if(nestedProps){
                buildNested(nestedProps)
            }
            return metaMapIncludes
        } else {
            return null
        }
    }

    boolean propertyExists(String propName){
        if (persistentEntity && persistentEntity.getPropertyByName(propName)){
            return true
        } else {
            def prop = PropertyTools.getMetaBeanProp(entityClass, propName)
            if(prop) return true
        }
    }

    //will recursivily call build and add to the metaMapIncludes
    MetaMapIncludes buildNested(Map<String, Object> nestedProps){

        // now we cycle through the nested props and recursively call this again for each associations includes
        Map<String, MetaMapIncludes> nestedIncludesMap = [:]
        for (entry in nestedProps.entrySet()) {
            String prop = entry.key as String //the nested property name
            Map initMap = entry.value as Map
            List incProps = initMap['props'] as List
            String assocClass = initMap['className'] as String
            MetaMapIncludes nestedIncludes

            if(assocClass) {
                nestedIncludes = build(assocClass, incProps)
            }
            // if no class then it wasn't a gorm association or gorm prop didn't have type
            // so try by getting value through meta reflection
            else {
                Class entityClass = ClassUtils.loadClass(entityClassName)
                Class returnType = PropertyTools.getPropertyReturnType(entityClass, prop)
                //if returnType is null at this point then the prop is bad or does not exist.
                //we allow bad props and just continue.
                if(returnType == null) {
                    continue
                }
                // else if its a collection
                else if(Collection.isAssignableFrom(returnType)){
                    String genClass = PropertyTools.findGenericForCollection(entityClass, prop)
                    if(genClass) {
                        nestedIncludes = build(genClass, incProps)
                    }
                    //TODO shouldn't we do at leas na object here? should not matter
                } else {
                    nestedIncludes = build(returnType.name, incProps)
                }
            }
            //if it got valid nestedIncludes and its notalready setup
            if(nestedIncludes && !nestedIncludesMap[prop]) nestedIncludesMap[prop] = nestedIncludes
        }

        if(nestedIncludesMap) metaMapIncludes.nestedIncludes = nestedIncludesMap

        return metaMapIncludes
    }

    static Set<String> getBlacklist(PersistentEntity entity){
        if(!entity) return [] as Set<String>
        String clazz = entity.name
        Set<String> blacklist = BLACKLIST.get(clazz)
        if(!blacklist){
            Map<String, ConstrainedProperty> constraints = GormMetaUtils.findAllConstrainedProperties(entity)
            blacklist = constraints.findAll{
                !it.value.isDisplay()
            }*.key as Set<String>
            BLACKLIST.put(clazz, blacklist)
        }
        return blacklist
    }


}
