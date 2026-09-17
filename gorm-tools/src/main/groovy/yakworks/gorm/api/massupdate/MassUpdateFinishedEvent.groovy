/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.massupdate

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import yakworks.api.ApiResults

/**
 * Fired once after all the items in a mass update have been processed, whether they succeeded or not.
 * This is the spot to hang batch level side effects such as creating a single activity for the run.
 *
 * @param <D> the entity domain class
 */
@CompileStatic
class MassUpdateFinishedEvent<D> extends ApplicationEvent implements ResolvableTypeProvider {

    Class<D> entityClass
    MassUpdateArgs args
    ApiResults results
    Boolean ok

    MassUpdateFinishedEvent(Object source, Class<D> entityClass, MassUpdateArgs args, ApiResults results) {
        super(source)
        this.entityClass = entityClass
        this.args = args
        this.results = results
        this.ok = results.ok
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    }
}
