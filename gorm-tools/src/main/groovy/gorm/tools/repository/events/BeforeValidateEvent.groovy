/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo

/**
 * BeforeValidateEvent is fired from the beforeValidate method as its more reliably called
 * when child associations are being validated in by grail's gorm
 */
@CompileStatic
class BeforeValidateEvent<D> extends RepositoryEvent<D> {

    BeforeValidateEvent(GormRepo<D> source, D entity, Map args) {
        super(source, entity, RepositoryEventType.BeforeValidate.eventKey, args)
    }

}
