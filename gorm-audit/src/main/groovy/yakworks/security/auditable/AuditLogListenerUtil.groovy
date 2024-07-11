/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.ToMany

import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.PersistentEntityValidator
import grails.util.Holders

/**
 * Provides AuditLogListener utilities that
 * can be used as either templates for your own extensions to
 * the plugin or as default utilities.
 */
@Slf4j
@CompileStatic
class AuditLogListenerUtil {

    /**
     * Get the original or persistent or original value for the given domain.property. This method includes
     * some special case handling for hasMany properties, which don't follow normal rules.
     *
     * By default, getPersistentValue() is used to obtain the value.
     * If the value is always NULL, you can set AuditLogConfig.usePersistentDirtyPropertyValue = false
     * In this case, DirtyCheckable.html#getOriginalValue() is used.
     *
     * @see GormEntity#getPersistentValue(java.lang.String)
     * @see org.grails.datastore.mapping.dirty.checking.DirtyCheckable#getOriginalValue(java.lang.String)
     */
    static Object getOriginalValue(Auditable domain, String propertyName) {
        PersistentEntity entity = getPersistentEntity(domain)
        PersistentProperty property = entity.getPropertyByName(propertyName)
        ConfigObject config = AuditLoggingConfigUtils.getAuditConfig()
        boolean usePersistentDirtyPropertyValues = config.getProperty("usePersistentDirtyPropertyValues")
        if (usePersistentDirtyPropertyValues) {
            property instanceof ToMany ? "N/A" : ((GormEntity)domain).getPersistentValue(propertyName)
        }
        else {
            property instanceof ToMany ? "N/A" : ((GormEntity)domain).getOriginalValue(propertyName)
        }
    }

    /**
     * Helper method to make a map of the current property values
     *
     * @param propertyNames
     * @param domain
     * @return
     */
    static Map<String, Object> makeMap(Collection<String> propertyNames, Auditable domain) {
        propertyNames.collectEntries { [it, domain.metaClass.getProperty(domain, it)] }
    }

    /**
     * Return the grails domain class for the given domain object.
     *
     * @param domain the domain instance
     */
    static PersistentEntity getPersistentEntity(domain) {
        Holders.grailsApplication.mappingContext.getPersistentEntity(domain.getClass().name)
    }

    /**
     * @param domain the auditable domain object
     * @param propertyName property name
     * @param value the value of the property
     * @return
     */
    static String conditionallyMaskAndTruncate(Auditable domain, String propertyName, String value, Integer maxLength) {
        if (value == null) {
            return null
        }

        // Always trim any space
        value = value.trim()

        if (domain.logMaskProperties && domain.logMaskProperties.contains(propertyName)) {
            return AuditLogContext.context.propertyMask as String ?: '********'
        }
        if (maxLength && value.length() > maxLength) {
            return value.substring(0, maxLength)
        }

        value
    }

    /**
     * Determine the truncateLength based on the configured truncateLength and the actual auditDomainClass maxSize constraint for newValue.
     */
    static Integer determineTruncateLength() {

        //todo, we dont need to do it like this, we can configure it in config ?
        /*
        String confAuditDomainClassName = AuditLoggingConfigUtils.auditConfig.getProperty('auditDomainClassName')
        if (!confAuditDomainClassName) {
            throw new IllegalArgumentException("Please configure auditLog.auditDomainClassName in Config.groovy")
        }

        MappingContext mappingContext = Holders.grailsApplication.mappingContext
        PersistentEntity auditPersistentEntity = mappingContext.getPersistentEntity(confAuditDomainClassName)
        if (!auditPersistentEntity) {
            throw new IllegalArgumentException("The configured audit logging domain class '$confAuditDomainClassName' is not a domain class")
        }

        // Get the property constraints
        PersistentEntityValidator entityValidator = mappingContext.getEntityValidator(auditPersistentEntity) as PersistentEntityValidator
        Map<String, ConstrainedProperty> constrainedProperties = (entityValidator?.constrainedProperties ?: [:]) as Map<String, ConstrainedProperty>

        // The configured length is the min size of oldValue, newValue, or configured truncateLength
        Integer oldValueMaxSize = constrainedProperties['oldValue'].maxSize ?: 255
        Integer newValueMaxSize = constrainedProperties['newValue'].maxSize ?: 255
        Integer maxSize = Math.min(oldValueMaxSize, newValueMaxSize)
        Integer configuredTruncateLength = (AuditLoggingConfigUtils.auditConfig.getProperty('truncateLength') ?: Integer.MAX_VALUE) as Integer

         */


        Integer.MAX_VALUE
    }
}
