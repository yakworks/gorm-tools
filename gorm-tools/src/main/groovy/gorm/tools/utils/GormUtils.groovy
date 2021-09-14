/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.Identity

import gorm.tools.beans.AppCtx
import gorm.tools.databinding.EntityMapBinder
import gorm.tools.model.Persistable
import yakworks.commons.lang.Validate

/**
 * GormUtils provides a set of static helpers for working with domain classes.
 * It allows to copy domain instances, to copy separate properties of an object, etc.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class GormUtils {

    /**
     * The list of domain properties which are ignored during copying.
     */
    static final List<String> IGNORED_PROPERTIES = ["id", "version", "createdBy", "createdDate", "editedBy", "editedDate", "num"]

    /**
     * Creates an instance of a given domain class and copies properties from source object.
     *
     * @param domainClass Domain class
     * @param source - domain class to copy properties from
     * @param override - properties to override after copying
     * @param ignoreAssociations - should associations be copied ? - ignored by default
     */
    static <T> T copyDomain(Class<T> domainClass, Object source, Map override = [:], boolean ignoreAssociations = true) {
        T copy = domainClass.newInstance()
        return copyDomain(copy, source, override, ignoreAssociations) as T
    }

    /**
     * Copies properties from source to target object.
     *
     * @param target domain instance to copy properties to
     * @param source - domain class to copy properties from
     * @param override - properties to override after copying
     * @param ignoreAssociations - should associations be copied ? - ignored by default
     */
    static Object copyDomain(Object target, Object source, Map override = [:], boolean ignoreAssociations = true) {
        if (target == null) throw new IllegalArgumentException("Target is null")
        if (source == null) return null

        //@formatter:off
        GormMetaUtils.getPersistentEntity(target.class).persistentProperties.each { PersistentProperty dp ->
            if (IGNORED_PROPERTIES.contains(dp.name) || dp instanceof Identity) return
            if (ignoreAssociations && dp instanceof Association) return

            String name = dp.name
            target[name] = source[name]
        }

        if (override) {
            setPropertiesFromMap(target, override)
        }

        return target
    }

    /**
     * Faster, simplier binder. Copies properties from source to target object.
     *
     * @param target domain instance to copy properties to
     * @param source - domain class to copy properties from
     * @param override - properties to override after copying
     * @param ignoreAssociations - should associations be copied ? - ignored by default
     */
    @CompileStatic
    static <T> GormEntity<T> bind(GormEntity<T> target, Map<String, Object> source, Map<String, Object> override = [:], boolean ignoreAssociations = false) {
        EntityMapBinder binder = (EntityMapBinder) AppCtx.get("entityMapBinder")
        binder.bind(target, source)

        if (override) {
            setPropertiesFromMap(target, override)
        }

        return target
    }

    /**
     * iterates over map and sets property to target, no conversion or error catching, just a straight forward set
     * if target.hasProperty for the key
     */
    static void setPropertiesFromMap(Object target, Map map){

        map.each { key, val ->
            String skey = key as String
            if (target.hasProperty(skey)) {
                target[skey] = val
            }
        }
    }

    /**
     * Copy all given property values from source to target
     * if and only if target's properties are null.
     *
     * @param source a source object
     * @param target a target object
     * @param propNames array of property names which should be copied
     */
    static void copyProperties(Object source, Object target, String... propNames) {
        copyProperties(source, target, true, propNames)
    }

    /**
     * Copy all given property values from source to target.
     * It can be specified whether to copy values or not in case target's properties are not null.
     *
     * @param source a source object
     * @param target a target object
     * @param copyOnlyIfNull if 'true' then it will copy a value only if target's property is null
     * @param propNames an array of property names which should be copied
     */
    static void copyProperties(Object source, Object target, boolean copyOnlyIfNull, String... propNames) {
        for (String prop : propNames) {
            if (copyOnlyIfNull && (target[prop] != null)) {
                continue
            } else {
                target[prop] = source[prop]
            }
        }

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
            if (obj != null) value = obj[prop]
            return value
        }

        return result
    }

    /**
     * simple util that collects the ids as Long list, can pass in idPropName for another field
     */
    static List<Long> collectLongIds(List dataList, String idPropName = 'id'){
        if(!dataList) return []
        return dataList.collect { it[idPropName] as Long }
    }

    /**
     * converts a list of long ids to a list of maps with id key
     * so [1,2,3] would be converted to [[id:1], [id:2], [id:3]]
     * can also pass in a different key with idPropName
     */
    static List<Map> listToIdMap(List<Long> idList, String idPropName = 'id'){
        idList.collect { [(idPropName): it] } as List<Map>
    }

    /**
     * converts a list of objects with id to a list of maps with id key
     * so [1,2,3] would be converted to [[id:1], [id:2], [id:3]]
     * can also pass in a different key with idPropName
     */
    static List<Map> entityListToIdMap(List entityList){
        entityList.collect { [id: it['id']] } as List<Map>
    }

}
