/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import yakworks.api.ApiResults
import yakworks.gorm.api.massupdate.MassUpdateArgs

/**
 * Fired after a mass update finishes (success or partial failure).
 * Hook for side effects such as creating one activity for the batch.
 */
@CompileStatic
class AfterMassUpdateEvent extends ApplicationEvent implements ResolvableTypeProvider {

    Class entityClass
    MassUpdateArgs args
    ApiResults results

    AfterMassUpdateEvent(Object source, Class entityClass, MassUpdateArgs args, ApiResults results) {
        super(source)
        this.entityClass = entityClass
        this.args = args
        this.results = results
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    }
}
