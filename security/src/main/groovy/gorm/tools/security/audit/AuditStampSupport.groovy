/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit

import java.time.LocalDateTime
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotationUtils

import gorm.tools.repository.GormRepoEntity
import gorm.tools.security.audit.ast.AuditStampConfigLoader
import gorm.tools.security.audit.ast.FieldProps
import gorm.tools.security.services.SecService
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.util.GrailsClassUtils

@CompileStatic
class AuditStampSupport {
    private static final String DISABLE_AUDITSTAMP_FIELD = 'disableAuditStamp'

    @Autowired GrailsApplication grailsApplication
    @Autowired SecService secService

    Map<String, FieldProps> fieldProps
    final Set<String> auditStampedEntities = [] as Set

    @PostConstruct
    void init() {
        if(!fieldProps) fieldProps = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())
        println "GormToolsAuditStampListener Init"
        GrailsClass[] domains = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
        for (GrailsClass domain : domains) {
            if (isClassAuditStamped(domain.clazz)) auditStampedEntities << domain.clazz.name
        }
        //initCurrentUserClosure()
    }

    //check if the given domain class should be audit stamped.
    boolean isClassAuditStamped(Class domainClass) {
        return ( AuditStampTrait.isAssignableFrom(domainClass) || AnnotationUtils.findAnnotation(domainClass, AuditStamp)) &&
            !isAuditStampDisabled(domainClass)
    }

    boolean isAuditStampDisabled(Class clazz) {
        GrailsClassUtils.getStaticPropertyValue(clazz, DISABLE_AUDITSTAMP_FIELD) == true
    }

    boolean isAuditStamped(String entityName){
        auditStampedEntities.contains(entityName)
    }

    /**
     * initialize stamp fields if need be
     *
     * @param entity
     */
    void stampIfNew(Object entity, Class<?> dateTimeClass = LocalDateTime) {
        //if its not new then just exit as we will assume an updated entity is initialized correctly
        if (!isNewEntity(entity)) return
        stampDefaults(entity, dateTimeClass)
    }

    void stampDefaults(Object entity, Class<?> dateTimeClass = LocalDateTime) {
        Object timestamp = createTimestamp(dateTimeClass)
        //assume its a new entity
        [FieldProps.CREATED_DATE_KEY, FieldProps.EDITED_DATE_KEY].each { key ->
            String datePropName = fieldProps[key].name
            entity[datePropName] = timestamp
        }
        Serializable uid = getCurrentUserId()
        [FieldProps.CREATED_BY_KEY, FieldProps.EDITED_BY_KEY].each { key ->
            String userPropName = fieldProps[key].name
            entity[userPropName] =  uid
        }
    }

    void stampUpdate(Object entity, Class<?> dateTimeClass = LocalDateTime) {
        Object timestamp = createTimestamp(dateTimeClass)
        String datePropName = fieldProps[FieldProps.EDITED_DATE_KEY].name
        String userPropName = fieldProps[FieldProps.EDITED_BY_KEY].name
        entity[datePropName] = timestamp
        entity[userPropName] =  getCurrentUserId()
    }

    void stampPreUpdateEvent(PreUpdateEvent event, Class<?> dateTimeClass = LocalDateTime) {
        Object timestamp = createTimestamp(dateTimeClass)
        EntityAccess ea = event.entityAccess
        String datePropName = fieldProps[FieldProps.EDITED_DATE_KEY].name
        String userPropName = fieldProps[FieldProps.EDITED_BY_KEY].name
        ea.setProperty(datePropName, timestamp)
        ea.setProperty(userPropName, getCurrentUserId())
    }

    public <T> T createTimestamp(Class<?> dateTimeClass = LocalDateTime) {
        return (T) DefaultGroovyMethods.invokeMethod(dateTimeClass, "now", null);
    }

    /**
     * Checks if the given domain instance is new
     *
     * it first checks for the createdDate property, if property exists and is not null, returns false, true if null
     * else If createdDate property is not defined, it checks if the domain is attached to session and exists in persistence context.
     *
     * @param entity
     * @return boolean
     */
    boolean isNewEntity(Object entity) {
        String createdDateFieldName = fieldProps[FieldProps.CREATED_DATE_KEY].name
        MetaProperty createdDateProperty = entity.hasProperty(createdDateFieldName)

        //see issue#41
        if(createdDateProperty != null) {
            def existingValue = createdDateProperty.getProperty(entity)
            return (existingValue == null)
        }
        // else {
        //     def session = applicationContext.sessionFactory.currentSession
        //     def entry = session.persistenceContext.getEntry(entity)
        //     return !entry
        // }
    }

    Serializable getCurrentUserId() {
        Serializable uid = secService.getUserId()
        return uid ?: 0L
    }

}
