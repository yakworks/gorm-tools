package gorm.tools.dao.events

import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("FieldName")
enum DaoEventType {

    BeforeRemove(BeforeRemoveEvent),
    BeforeCreate(BeforeCreateEvent),
    BeforeUpdate(BeforeUpdateEvent),
    BeforePersist(BeforePersistEvent),
    AfterRemove(AfterRemoveEvent),
    AfterCreate(AfterCreateEvent),
    AfterUpdate(AfterUpdateEvent),
    AfterPersist(AfterPersistEvent)

    Class eventClass
    String eventKey

    DaoEventType(Class clazz) {
        this.eventClass = clazz
        this.eventKey = GrailsNameUtils.getPropertyName(name())
    }
}
