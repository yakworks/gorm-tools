/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.massupdate

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import yakworks.api.ApiResults
import yakworks.gorm.api.massupdate.MassUpdateArgs

/**
 * Fired once after all the items in a mass update have been processed, whether they succeeded or not.
 * Published after optional activity creation.
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
