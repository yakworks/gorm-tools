/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.api.RepositoryApi

/**
 * Fired before a bind and save/persist. Allows modification to params used in the binding
 *
 * @param D the entity domain class
 */
@CompileStatic
class BeforeBindEvent<D> extends RepositoryEvent<D> {

    BeforeBindEvent(RepositoryApi source, D entity, Map data, String bindAction) {
        super(source, entity, RepositoryEventType.BeforeBind.eventKey)
        this.data = data
        this.bindAction = bindAction
    }

}
