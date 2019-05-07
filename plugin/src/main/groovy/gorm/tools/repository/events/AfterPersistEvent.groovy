/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired after successful repository.persist
 */
@CompileStatic
class AfterPersistEvent<D> extends RepositoryEvent<D> {

    /** the args passed into persist */
    Map args

    AfterPersistEvent(RepositoryApi repo, D entity, Map args) {
        super(repo, entity, RepositoryEventType.AfterPersist.eventKey)
        this.args = args
        //setDataFromArgMap(args)
    }

    Map getData(){
        args ? args['data'] as Map : null
    }

    String getBindAction(){
        args ? args['bindAction'] as String : null
    }

}
