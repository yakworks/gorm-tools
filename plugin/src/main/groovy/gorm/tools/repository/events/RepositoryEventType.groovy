package gorm.tools.repository.events

import grails.util.GrailsNameUtils
import groovy.transform.CompileStatic

@CompileStatic
@SuppressWarnings("FieldName")
enum RepositoryEventType {

    BeforeRemove(BeforeRemoveEvent),
    BeforeBind(BeforeBindEvent),
    BeforePersist(BeforePersistEvent),
    AfterRemove(AfterRemoveEvent),
    AfterBind(AfterBindEvent),
    AfterPersist(AfterPersistEvent)

    Class eventClass
    String eventKey

    RepositoryEventType(Class clazz) {
        this.eventClass = clazz
        this.eventKey = GrailsNameUtils.getPropertyName(name())
    }
}
