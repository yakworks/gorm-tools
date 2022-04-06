/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import yakworks.commons.lang.Validate

class SyncJobFinishedEvent<D> extends ApplicationEvent implements ResolvableTypeProvider   {

    Long jobId
    Boolean ok
    SyncJobContext context
    Class domainClass

    SyncJobFinishedEvent(SyncJobContext ctx) {
        super(ctx)
        this.context = ctx
        this.jobId = ctx.jobId
        this.ok = ctx.ok.get()
        this.domainClass = ctx.args.domainClass
    }

    static SyncJobFinishedEvent of(SyncJobContext ctx){
        Validate.notNull(ctx, "syncJobContext is null")
        Validate.notNull(ctx.args.domainClass, "syncJobContext.args.domainClass is null")
        return new SyncJobFinishedEvent(ctx)
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(domainClass))
    }
}
