package gorm.tools.repository.events

import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("FieldName")
enum RepositoryEventType {

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

    RepositoryEventType(Class clazz) {
        this.eventClass = clazz
        this.eventKey = GrailsNameUtils.getPropertyName(name())
    }
}
