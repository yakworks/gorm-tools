/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import gorm.tools.repository.api.RepositoryApi
import groovy.transform.CompileStatic

/**
 * Fired right before enitity save inside repository.persist
 */
@CompileStatic
class BeforePersistEvent<D> extends RepositoryEvent<D> {

    /** the args passed into persist */
    Map args

    BeforePersistEvent(RepositoryApi source, D entity, Map args) {
        super(source, entity, RepositoryEventType.BeforePersist.eventKey)
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
