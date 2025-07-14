/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job.events

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import gorm.tools.job.SyncJobContext
import gorm.tools.job.SyncJobState
import yakworks.commons.lang.Validate

/**
 * Was SyncJobStartEvent.
 * Event fired on State change.
 * @param <D> the entity class this is for
 */
@CompileStatic
class SyncJobStateEvent extends ApplicationEvent { //implements ResolvableTypeProvider {

    Long jobId
    SyncJobState state
    SyncJobContext context

    SyncJobStateEvent(Long jobId, SyncJobContext ctx, SyncJobState state) {
        super(ctx)
        this.context = ctx
        this.jobId = ctx.jobId
        //this.entityClass = ctx.args.entityClass ?: Object //if no entityClass - eg for exportSync
    }

    static SyncJobStateEvent of(Long jobId, SyncJobContext ctx, SyncJobState state) {
        Validate.notNull(ctx, "syncJobContext is null")
        return new SyncJobStateEvent(jobId, ctx, state)
    }

    // @Override
    // ResolvableType getResolvableType() {
    //     return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    // }
}
