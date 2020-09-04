/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.stamp

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

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

import gorm.tools.AuditStamp
import gorm.tools.compiler.stamp.FieldProps
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugin.rally.security.SecService
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
        GrailsClass[] domains = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)
        for (GrailsClass domain : domains) {
            if (isAuditStamped(domain.clazz)) auditStampedEntities << domain.clazz.name
        }
        //initCurrentUserClosure()
    }

    //check if the given domain class should be audit stamped.
    boolean isAuditStamped(Class domainClass) {
        return AnnotationUtils.findAnnotation(domainClass, AuditStamp) && !isAuditStampDisabled(domainClass)
    }

    boolean isAuditStampDisabled(Class clazz) {
        GrailsClassUtils.getStaticPropertyValue(clazz, DISABLE_AUDITSTAMP_FIELD) == true
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        EntityAccess ea = event.entityAccess
        PersistentEntity entity = event.entity

        if (entity == null || !auditStampedEntities.contains(entity.name)) return

        if (event.getEventType() == EventType.PreInsert) beforeInsert(ea)
        else if (event.getEventType() == EventType.PreUpdate) beforeUpdate(ea)
        else if (event.getEventType() == EventType.Validation) beforeValidate(ea)

    }

    private void beforeInsert(EntityAccess ea) {
        setDateField(FieldProps.CREATED_DATE_KEY, ea)
        setUserField(FieldProps.CREATED_BY_KEY, ea)
        setDateField(FieldProps.EDITED_DATE_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    private void beforeUpdate(EntityAccess ea) {
        setDateField(FieldProps.EDITED_DATE_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    private void beforeValidate(EntityAccess ea) {
        if (isNewEntity(ea)) {
            setDefaults(ea)
        }
    }

    void setDateField(String prop, EntityAccess ea, Date date = new Date()) {
        ea.setProperty(fieldProps[prop].name, date)
    }

    void setUserField(String prop, EntityAccess ea) {
        ea.setProperty(fieldProps[prop].name, currentUserId)
    }

    void setDefaults(EntityAccess ea) {
        Date now = new Date()

        setDateField(FieldProps.CREATED_DATE_KEY, ea, now)
        setDateField(FieldProps.EDITED_DATE_KEY, ea, now)

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
    private boolean isNewEntity(EntityAccess ea) {
        String createdDateFieldName = fieldProps.get(FieldProps.CREATED_DATE_KEY).name
        def value = ea.getPropertyValue(createdDateFieldName)
        return value == null
    }

    Serializable getCurrentUserId() {
        return secService.getUserId()
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.isAssignableFrom(eventType) ||
                PreUpdateEvent.isAssignableFrom(eventType) ||
                ValidationEvent.isAssignableFrom(eventType)
    }
}
