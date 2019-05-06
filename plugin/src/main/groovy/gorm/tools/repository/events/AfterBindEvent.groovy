/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired After a bind and save/persist. Often used along with params to setup other domains associations
 *
 * @param D the entity domain class
 */
@CompileStatic
class AfterBindEvent<D> extends RepositoryEvent<D> {

    AfterBindEvent(RepositoryApi repo, D entity, Map data, String bindAction) {
        super(repo, entity, RepositoryEventType.AfterBind.eventKey)
        this.data = data
        this.bindAction = bindAction
    }

}
