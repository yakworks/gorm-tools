/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.engine.event.ValidationEvent
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEvent
import org.springframework.core.annotation.AnnotationUtils

import gorm.tools.security.audit.ast.AuditStampConfigLoader
import gorm.tools.security.audit.ast.FieldProps
import gorm.tools.security.services.SecService
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.util.GrailsClassUtils

@CompileStatic
class GormToolsAuditStampListener extends AbstractPersistenceEventListener {
    private static final String DISABLE_AUDITSTAMP_FIELD = 'disableAuditTrailStamp'


    @Autowired GrailsApplication grailsApplication
    @Autowired SecService secService

    final Set<String> auditStampedEntities = [] as Set
    Map<String, FieldProps> fieldProps

    protected GormToolsAuditStampListener(Datastore datastore) {
        super(datastore)
    }

    @PostConstruct
    void init() {
        if(!fieldProps) fieldProps = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())
        println "GormToolsAuditStampListener Init datastore: $datastore"
        GrailsClass[] domains = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
        for (GrailsClass domain : domains) {
            if (isAuditStamped(domain.clazz)) auditStampedEntities << domain.clazz.name
        }
        //initCurrentUserClosure()
    }

    //check if the given domain class should be audit stamped.
    boolean isAuditStamped(Class domainClass) {
        return ( AuditStampTrait.isAssignableFrom(domainClass) || AnnotationUtils.findAnnotation(domainClass, AuditStamp)) &&
            !isAuditStampDisabled(domainClass)
    }

    boolean isAuditStampDisabled(Class clazz) {
        GrailsClassUtils.getStaticPropertyValue(clazz, DISABLE_AUDITSTAMP_FIELD) == true
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        EntityAccess ea = event.entityAccess
        PersistentEntity entity = event.entity
        println "GormToolsAuditStampListener onPersistenceEvent ${event.eventType}"
        if (entity == null || !auditStampedEntities.contains(entity.name)) return

        if (event.getEventType() == EventType.PreInsert) beforeInsert(ea)
        else if (event.getEventType() == EventType.PreUpdate) beforeUpdate(ea)
    }

    private void beforeInsert(EntityAccess ea) {
        setDefaults(ea)
    }

    private void beforeUpdate(EntityAccess ea) {
        setTimestampField(FieldProps.EDITED_DATE_KEY, ea, null)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    void setTimestampField(String prop, EntityAccess ea, Object date) {
        String datePropName = fieldProps[prop].name
        Object timestamp = date
        if(!timestamp){
            Class<?> dateTimeClass = ea.getPropertyType(datePropName)
            timestamp = createTimestamp(dateTimeClass)
        }
        ea.setProperty(datePropName, timestamp)
    }

    public <T> T createTimestamp(Class<?> dateTimeClass) {
        return (T) DefaultGroovyMethods.invokeMethod(dateTimeClass, "now", null);
    }

    @Deprecated
    void setDateField(String prop, EntityAccess ea, Date date = new Date()) {
        ea.setProperty(fieldProps[prop].name, date)
    }

    void setUserField(String prop, EntityAccess ea) {
        ea.setProperty(fieldProps[prop].name, currentUserId)
    }

    void setDefaults(EntityAccess ea) {
        // LocalDateTime now = LocalDateTime.now()
        Class<?> dateTimeClass = ea.getPropertyType(fieldProps[FieldProps.CREATED_DATE_KEY].name)
        Object timestamp = createTimestamp(dateTimeClass)

        setTimestampField(FieldProps.CREATED_DATE_KEY, ea, timestamp)
        setTimestampField(FieldProps.EDITED_DATE_KEY, ea, timestamp)

        setUserField(FieldProps.CREATED_BY_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    /**
     * Checks if the given domain instance is new
     *
     * it first checks for the createdDate property, if property exists and is not null, returns false, true if null
     *
     * @param entity
     * @return boolean
     */
    private boolean isNewEntity(Object entity) {
        String createdDateFieldName = fieldProps.get(FieldProps.CREATED_DATE_KEY).name
        def value = entity[createdDateFieldName]
        return value == null
    }

    Serializable getCurrentUserId() {
        Serializable uid = secService.getUserId()
        return uid ?: 0L
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.isAssignableFrom(eventType) ||
                PreUpdateEvent.isAssignableFrom(eventType) ||
                ValidationEvent.isAssignableFrom(eventType)
    }
}
