/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap


import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.ToMany

import gorm.tools.utils.GormMetaUtils
import grails.gorm.validation.ConstrainedProperty
import yakworks.commons.beans.PropertyTools
import yakworks.gorm.api.IncludesConfig
import yakworks.meta.MetaEntity
import yakworks.meta.MetaProp

/**
 * Builder to create MetaEntity from a sql select like list against an entity
 */
@Slf4j
@CompileStatic
@SuppressWarnings('InvertedIfElse')
class MetaGormEntityBuilder {
    /**
     * Holds the list of fields that have display:false for a class, meaning they should not be exported
     */
    static final Map<String, Set<String>> BLACKLIST = new ConcurrentHashMap<String, Set<String>>()

    List<String> includes
    List<String> excludes = []
    String entityClassName
    Class entityClass
    PersistentEntity persistentEntity
    List<PersistentProperty> persistentProperties = []
    MetaEntity metaEntity


    MetaGormEntityBuilder(Class clazz){
        this.entityClass = clazz
        this.entityClassName = clazz.name
        init()
    }

    MetaGormEntityBuilder(String entityClassName, List<String> includes){
        this.entityClassName = entityClassName
        this.includes = includes ?: ['*'] as List<String>
        init()
    }

    MetaGormEntityBuilder includes(List<String> includes) {
        this.includes = includes ?: ['*'] as List<String>
        return this
    }

    MetaGormEntityBuilder excludes(List<String> val) {
        if(val != null) this.excludes = val
        return this
    }

    static MetaGormEntityBuilder of(Class entityClass){
        return new MetaGormEntityBuilder(entityClass)
    }

    static MetaEntity build(Class clazz, List<String> includes){
        return MetaGormEntityBuilder.of(clazz).includes(includes).build()
    }

    static MetaEntity build(String entityClassName, List<String> includes, List<String> excludes = []){
        def mmib = new MetaGormEntityBuilder(entityClassName, includes)
        if(excludes) mmib.excludes(excludes)
        return mmib.build()
    }

    void init(){
        this.persistentEntity = GormMetaUtils.findPersistentEntity(entityClassName)

        if(this.persistentEntity){
            //in case the short name was passed in make sure its full class name
            entityClassName = persistentEntity.javaClass.name
            persistentProperties = GormMetaUtils.getPersistentProperties(persistentEntity)
        }

        if(!entityClass && entityClassName) {
            ClassLoader classLoader = getClass().getClassLoader()
            this.entityClass = classLoader.loadClass(entityClassName)
        }
        this.metaEntity = new MetaEntity(entityClass)
    }

    /**
     * builds a EntityMapIncludes object from a sql select like list. Used in EntityMap and EntityMapList
     *
     * @param className the class name of the PersistentEntity
     * @param includes the includes list in our custom dot notation
     * @return the EntityMapIncludes object that can be passed to EntityMap
     */
    MetaEntity build() {
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
                    if(persistentProperties){
                        List props = persistentProperties.findAll {
                            !(it instanceof ToMany)
                        }

                        props.each { p ->
                            metaEntity.metaProps[p.name] = MetaProp.of(p.name, p.type)
                        }
                        // metaEntity.fields.addAll(props)
                    }
                }
                //if it start with a $ then use it as includesKey
                else if (field.startsWith('$')) {
                    String incKey = field.replace('$', '')
                    Map incsMap = IncludesConfig.bean().getIncludes(entityClass)
                    if(incsMap){
                        List props = ( incsMap[incKey] ?: ['id'] ) as List<String>
                        def toMerge = MetaGormEntityBuilder.build(entityClass.name, props)
                        metaEntity.merge(toMerge)
                    }
                }
                //just a normal prop but make sure it exists
                else {
                    MetaProp propm = getPropMeta(field)
                    if(propm){
                        metaEntity.metaProps[field] = propm
                    }
                    // TODO should add check for transient?
                }
            }
            else { // its a nestedProp

                //set it up if it has not been yet
                if (!nestedProps[nestedPropName]) {
                    PersistentProperty pp = persistentProperties.find { it.name == nestedPropName }
                    Class nestedClass
                    // check if its association
                    if (pp instanceof Association) {
                        nestedClass = (pp as Association)?.getAssociatedEntity()
                    } else {
                        nestedClass = pp?.type
                    }
                    Map<String, Object> initMap = ['className': nestedClass?.name, 'props': [] as Set]
                    nestedProps[nestedPropName] = initMap
                    //add a placeholder in the map to keep order
                    metaEntity.metaProps[nestedPropName] = new MetaProp(nestedPropName, nestedClass)
                }
                //if prop is foo.bar.baz then this get the bar.baz part
                String propPath = field.substring(nestedIndex + 1)

                // if its a $* then thats a hard override that we want *
                if(propPath == '$*') propPath = '$stamp'

                def initMap = nestedProps[nestedPropName] as Map<String, Object>
                (initMap['props'] as Set).add(propPath)
            }
        }
        //create the includes class for what we have now along with the the blacklist
        Set blacklist = getBlacklist(persistentEntity) + (this.excludes as Set)

        //if it doesn't have metaProps then Includes must be bad?
        //XXX WHY DO WE RETURN NULL?
        if (!metaEntity.metaProps)  return null //throw new IllegalArgumentException("includes must be bad, includes: ${includes}")

        if(blacklist) metaEntity.addBlacklist(blacklist)
        //if it has nestedProps then go recursive
        if(nestedProps){
            buildNested(nestedProps)
        }
        return metaEntity

    }

    /** PropMeta from propName depending on whether its a persistentEntity or normal bean */
    MetaProp getPropMeta(String propName){
        def perProp = persistentEntity?.getPropertyByName(propName)
        if (perProp){
            return MetaProp.of(perProp.name, perProp.type)
        } else {
            def mprop = PropertyTools.getMetaBeanProp(entityClass, propName)
            if(mprop) return new MetaProp(mprop)
        }
        return null
    }

    boolean propertyExists(String propName){
        if (persistentEntity && persistentEntity.getPropertyByName(propName)){
            return true
        } else {
            def prop = PropertyTools.getMetaBeanProp(entityClass, propName)
            if(prop) return true
        }
        return false
    }

    //will recursivily call build and add to the metaEntity
    MetaEntity buildNested(Map<String, Object> nestedProps){

        // now we cycle through the nested props and recursively call this again for each associations includes
        Map<String, MetaEntity> metaEntityMetaProps = [:]
        for (entry in nestedProps.entrySet()) {
            String prop = entry.key as String //the nested property name
            Map initMap = entry.value as Map
            List incProps = initMap['props'] as List
            String assocClass = initMap['className'] as String
            MetaEntity nestedMetaEntity

            if(assocClass) {
                nestedMetaEntity = build(assocClass, incProps)
            }
            // if no class then it wasn't a gorm association or gorm prop didn't have type
            // so try by getting value through meta reflection
            else {
                ClassLoader classLoader = getClass().getClassLoader()
                Class entityClass = classLoader.loadClass(entityClassName)
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
                        nestedMetaEntity = MetaGormEntityBuilder.build(genClass, incProps)
                    }
                    //TODO shouldn't we do at leas na object here? should not matter
                } else {
                    nestedMetaEntity = MetaGormEntityBuilder.build(returnType.name, incProps)
                }
            }
            //if it got valid metaEntityProps and its not already setup
            if(nestedMetaEntity && !metaEntityMetaProps[prop]) metaEntityMetaProps[prop] = nestedMetaEntity
        }

        if(metaEntityMetaProps) {
            metaEntity.metaProps.putAll(metaEntityMetaProps)
        }

        return metaEntity
    }

    @CompileDynamic
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
