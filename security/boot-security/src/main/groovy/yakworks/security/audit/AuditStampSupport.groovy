/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.audit

import java.time.LocalDateTime
import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.DefaultGroovyMethods
import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.annotation.AnnotationUtils

import gorm.tools.utils.GormMetaUtils
import yakworks.commons.lang.ClassUtils
import yakworks.security.audit.ast.FieldProps
import yakworks.security.user.UserInfo

/**
 * support service for making sure entites are stamped with appropriate fields
 */
@CompileStatic
class AuditStampSupport {
    private static final String DISABLE_AUDITSTAMP_FIELD = 'disableAuditStamp'
    //static accessor for the getUserInfo used in AuditStampTrait
    protected static AuditUserResolver USER_RESOLVER

    @Autowired AuditUserResolver auditUserResolver
    @Autowired MappingContext grailsDomainClassMappingContext

    Map<String, FieldProps> fieldProps
    final Set<String> auditStampedEntities = [] as Set

    @PostConstruct
    void init() {
        // if(!fieldProps) fieldProps = FieldProps.buildFieldMap(new AuditStampConfigLoader().load())
        if(!fieldProps) fieldProps = FieldProps.buildFieldMap([:])
        for (PersistentEntity persistentEntity : grailsDomainClassMappingContext.getPersistentEntities()) {
            if (isClassAuditStamped(persistentEntity.javaClass)) auditStampedEntities << persistentEntity.name
        }
        USER_RESOLVER = auditUserResolver
        //initCurrentUserClosure()
    }

    // //inject so it has a static reference
    // @Autowired
    // void setAuditUserResolver(AuditUserResolver auditUserResolver){
    //     AuditStampSupport.USER_RESOLVER = auditUserResolver
    // }


    //check if the given domain class should be audit stamped.
    boolean isClassAuditStamped(Class domainClass) {
        return ( AuditStampTrait.isAssignableFrom(domainClass) || AnnotationUtils.findAnnotation(domainClass, AuditStamp)) &&
            !isAuditStampDisabled(domainClass)
    }

    boolean isAuditStampDisabled(Class clazz) {
        ClassUtils.getStaticPropertyValue(clazz, DISABLE_AUDITSTAMP_FIELD, Boolean) == true
    }

    boolean isAuditStamped(String name){
        return auditStampedEntities.contains(GormMetaUtils.unwrapIfProxy(name))
    }

    /**
     * initialize stamp fields if need be
     */
    void stampIfNew(Object entity, Class<?> dateTimeClass = LocalDateTime) {
        //if its not new then just exit as we will assume an updated entity is initialized correctly
        // if (!isNewEntity(entity)) return
        // FIXME temp fix for bad data
        if (hasCreatedDate(entity)){
            //if it has createDate then its not new but might have bad data
            ensureEditedDate(entity)
        } else {
            stampDefaults(entity, dateTimeClass)
        }

    }

    void ensureEditedDate(Object entity) {
        //assumes that createdDateProperty != null at this point
        String createdDateName = fieldProps[FieldProps.CREATED_DATE_KEY].name
        String editedDateName = fieldProps[FieldProps.EDITED_DATE_KEY].name
        if(!entity[editedDateName]) {
            entity[editedDateName] = entity[createdDateName]
        }
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

    void stampPreUpdateEvent(EntityAccess ea, Class<?> dateTimeClass = LocalDateTime) {
        Object timestamp = createTimestamp(dateTimeClass)
        String datePropName = fieldProps[FieldProps.EDITED_DATE_KEY].name
        String userPropName = fieldProps[FieldProps.EDITED_BY_KEY].name
        ea.setProperty(datePropName, timestamp)
        ea.setProperty(userPropName, getCurrentUserId())
    }

    public <T> T createTimestamp(Class<?> dateTimeClass = LocalDateTime) {
        return (T) InvokerHelper.invokeStaticMethod(dateTimeClass, "now", null);
    }

    /**
     * Checks if the given domain instance is new
     *
     * it first checks for the createdDate property, if property exists and is not null, returns false, true if null
     * else If createdDate property is not defined, it checks if the domain is attached to session and exists in persistence context.
     */
    boolean hasCreatedDate(Object entity) {
        String createdDateFieldName = fieldProps[FieldProps.CREATED_DATE_KEY].name
        MetaProperty createdDateProperty = entity.hasProperty(createdDateFieldName)

        //see issue#41
        if(createdDateProperty != null) {
            def existingValue = createdDateProperty.getProperty(entity)
            return existingValue != null
        }
        // else {
        //     def session = applicationContext.sessionFactory.currentSession
        //     def entry = session.persistenceContext.getEntry(entity)
        //     return !entry
        // }
    }

    Serializable getCurrentUserId() {
        Serializable uid = USER_RESOLVER.getCurrentUserId()
        return uid ?: 0L
    }

    static UserInfo getUserInfo(Serializable uid) {
        return USER_RESOLVER.getUserInfo(uid)
    }

}
