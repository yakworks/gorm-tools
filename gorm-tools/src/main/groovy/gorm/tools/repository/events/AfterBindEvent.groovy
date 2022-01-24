/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs

/**
 * Fired After a bind and save/persist. Often used along with params to setup other domains associations
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBindEvent<D> extends RepositoryEvent<D> {

    AfterBindEvent(GormRepo<D> repo, D entity, Map data, BindAction bindAction, PersistArgs args) {
        super(repo, entity, RepositoryEventType.AfterBind.eventKey, data, bindAction, args)
    }

}
