/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.orm.hibernate.cfg.Mapping
import org.springframework.validation.Validator

import gorm.tools.beans.AppCtx
import gorm.tools.validation.ApiConstraints
import grails.gorm.validation.ConstrainedEntity
import grails.gorm.validation.ConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils

/**
 * A bunch of helper and lookup/finder statics for dealing with domain classes and PersistentEntity.
 * Useful methods to find the PersistentEntity and the mapping and meta fields.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class GormMetaUtils {

    /**
     * Returns a persistent entity using the class.
     *
     * @param cls the domain class
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(Class cls) {
        getMappingContext().getPersistentEntity(cls.name)
    }

    /**
     * Returns a persistent entity using a fully qualified name
     * with package such as "com.foo.Product".
     *
     * @param name an entity name which includes package
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(String fullName) {
        getMappingContext().getPersistentEntity(fullName)
    }

    /**
     * calls the static getter method getGormPersistentEntity on the instace
     * to get the PersistentEntity
     *
     * @param instance the domain instance
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(GormEntity instance) {
        return (PersistentEntity)ClassUtils.getStaticPropertyValue(instance.class, "gormPersistentEntity")
    }

    /**
     * finds domain using either a simple name like "Product" or fully qualified name "com.foo.Product"
     * name can start with either a lower or upper case
     *
     * @param name an entity name to search for
     * @return The entity or null
     */
    static PersistentEntity findPersistentEntity(String name) {
        if (name.indexOf('.') == -1) {
            String propertyName = NameUtils.getPropertyName(name)
            return getMappingContext().persistentEntities.find { PersistentEntity entity ->
                entity.decapitalizedName == propertyName
            }
        }
        return getPersistentEntity(name)
    }

    /**
     * the mapping grailsDomainClassMappingContext. This is the main holder for the persistentEntities
     */
    static MappingContext getMappingContext() {
        AppCtx.get("grailsDomainClassMappingContext", MappingContext)
    }

    /**
     * the mapping grailsDomainClassMappingContext. This is the main holder for the persistentEntities
     */
    static Object unwrap(Object entity) {
        getMappingContext().getProxyHandler().unwrap(entity)
    }

    /**
     * If the name has `_$$_` then it removes it then returns it. otherwise just returns the same name passed in
     */
    static String unwrapIfProxy(String name) {
        final int proxyIndicator = name.indexOf('_$$_')
        if (proxyIndicator > -1) {
            name = name.substring(0, proxyIndicator)
        }
        return name
    }

    /**
     * Returns the mapping for the entity to DB.
     *
     * @param pe the PersistentEntity can found using the loolup statics above
     * @return the mapping for the entity to DB
     */
    @CompileDynamic
    Mapping getMapping(PersistentEntity pe) {
        return getMappingContext().mappingFactory?.entityToMapping?.get(pe)
    }

    @CompileDynamic
    static Map<String, ConstrainedProperty> findConstrainedProperties(Class pe) {
        findConstrainedProperties(pe.getGormPersistentEntity())
    }

    static Map<String, ConstrainedProperty> findConstrainedProperties(PersistentEntity entity) {
        Validator validator = entity.getMappingContext().getEntityValidator(entity)
        if(validator instanceof ConstrainedEntity) {
            ConstrainedEntity constrainedEntity = (ConstrainedEntity)validator
            Map<String, ConstrainedProperty> constrainedProperties = constrainedEntity.getConstrainedProperties()
            return constrainedProperties
        }
        return Collections.emptyMap()
    }

    static Map<String, ConstrainedProperty> findNonValidatedProperties(PersistentEntity entity) {
        def apiConstraints = ApiConstraints.findApiConstraints(entity.javaClass)
        return apiConstraints ? apiConstraints.nonValidatedProperties : [:]
    }

    /**
     * returns both non-validated and validated Constrained Properties
     */
    static Map<String, ConstrainedProperty> findAllConstrainedProperties(PersistentEntity entity) {
        return findConstrainedProperties(entity) + findNonValidatedProperties(entity)
    }

    // static Map<String, Constrained> getConstrainedProperties(PersistentEntity entity) {
    //     return new DefaultConstrainedDiscovery().findConstrainedProperties(entity)
    // }

    /**
     * Check if Persistent Entity has property by path
     *
     * @param domain Persistent Entity
     * @param property path for property
     * @return true if there is such property, false othervise
     */
    @CompileDynamic
    static boolean hasProperty(PersistentEntity domain, String property) {
        Closure checkProperty
        checkProperty = { PersistentEntity domainClass, List path ->
            PersistentProperty prop = domainClass?.getPropertyByName(path[0].toString())
            if (path.size() > 1 && prop) {
                checkProperty(prop.associatedEntity, path.tail())
            } else {
                prop as boolean
            }
        }
        checkProperty(domain, property.split("[.]") as List)

    }

    /**
     * Returns persistent properties for persistent entity(finds by name)
     * Adds composite identeties, which are not in persistent properties by default
     *
     * @param className name of the Persistent entity
     * @return list of PersistentProperties, includes composite Identities and identity
     */
    static List<PersistentProperty> getPersistentProperties(String className){
        PersistentEntity domain = findPersistentEntity(className)
        getPersistentProperties(domain)
    }

    /**
     * Returns persistent properties for persistent entity(finds by name)
     * Adds composite identities, which are not in persistent properties by default
     *
     * @param className name of the Persistent entity
     * @return list of PersistentProperties, includes composite Identities and identity
     */
    static List<PersistentProperty> getPersistentProperties(PersistentEntity domain){
        List<PersistentProperty> result = domain.persistentProperties.collect() // collect copies it
        if(domain.compositeIdentity) result.addAll(domain.compositeIdentity)
        result.add(0, domain.getIdentity())
        result.unique()
    }

    /**
     * gets the id on the instance using entity reflector and trying not to init the proxy if its is one
     */
    static Serializable getId(GormEntity instance) {
        PersistentEntity persistentEntity = getPersistentEntity(instance)
        def proxyHandler = persistentEntity.mappingContext.proxyHandler
        if(proxyHandler.isProxy(instance)) {
            return proxyHandler.getIdentifier(instance)
        }
        else {
            persistentEntity.mappingContext.getEntityReflector(persistentEntity).getIdentifier(instance)
        }
    }

    /**
     * gets a Map representing the id key trying not to init the proxy if its is one.
     * see getId helper here as well.
     * If no defaults changed and id field is named id and is a of Long type
     * its would return '[id: 123]' as an example.
     */
    static Map getIdMap(GormEntity instance) {
        PersistentEntity persistentEntity = getPersistentEntity(instance)
        Serializable idVal = getId(instance)
        String idName = persistentEntity.identity.name
        return  [(idName): idVal]
    }

}
