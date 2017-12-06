package gorm.tools.dao.events

import groovy.transform.CompileStatic;

@CompileStatic
enum DaoEventType {

    BeforeRemove(PreDaoRemoveEvent),
    BeforeCreate(PreDaoCreateEvent),
    BeforeUpdate(PreDaoUpdateEvent),
    AfterRemove(PostDaoRemoveEvent),
    AfterCreate(PostDaoCreateEvent),
    AfterUpdate(PostDaoUpdateEvent),
    BeforePersist(PreDaoPersistEvent),
    AfterPersist(PostDaoPersistEvent)

    Class eventClass

    DaoEventType(Class clazz) {
        this.eventClass = clazz
    }
}
