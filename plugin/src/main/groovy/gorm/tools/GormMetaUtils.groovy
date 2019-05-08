/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.orm.hibernate.cfg.Mapping
import org.springframework.validation.Validator

import gorm.tools.beans.AppCtx
import grails.gorm.validation.ConstrainedEntity
import grails.gorm.validation.ConstrainedProperty
import grails.util.GrailsNameUtils

/**
 * A bunch of helper and lookup/finder statics for dealing with domain classes and PersistentEntity.
 * Useful methods to find the PersistentEntity and the mapping and meta fields.
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
     * Returns a persistent entity using a domain instance.
     *
     * Note: shortcut for getPersistentEntity(instance.class.name)
     *
     * @param instance the domain instance
     * @return The entity or null
     */
    static PersistentEntity getPersistentEntity(GormEntity instance) {
        getPersistentEntity(instance.class.name)
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
            String propertyName = GrailsNameUtils.getPropertyName(name)
            return getMappingContext().persistentEntities.find { PersistentEntity entity ->
                entity.decapitalizedName == propertyName
            }
        }
        return getPersistentEntity(name)
    }

    /**
     * the mapping grailsDomainClassMappingContext. This is the main holder for the persistentEntities
     * @return
     */
    static MappingContext getMappingContext() {
        AppCtx.get("grailsDomainClassMappingContext", MappingContext)
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

    static Map<String, ConstrainedProperty> findConstrainedProperties(PersistentEntity entity) {
        Validator validator = entity.getMappingContext().getEntityValidator(entity)
        if(validator instanceof ConstrainedEntity) {
            ConstrainedEntity constrainedEntity = (ConstrainedEntity)validator
            Map<String, ConstrainedProperty> constrainedProperties = constrainedEntity.getConstrainedProperties()
            return constrainedProperties
        }
        return Collections.emptyMap()
    }

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
        List<PersistentProperty> result = domain.persistentProperties
        if(domain.compositeIdentity) result.addAll(domain.compositeIdentity)
        result.add(0, domain.getIdentity())
        result.unique()
    }

}
