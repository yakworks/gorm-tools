/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.job

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

class SyncJobStartEvent<D> extends ApplicationEvent implements ResolvableTypeProvider {

    Long jobId
    SyncJobContext context
    Class domainClass //domain class for which this sync job is

    SyncJobStartEvent(SyncJobContext ctx) {
        super(ctx)
        this.context = ctx
        this.jobId = ctx.jobId
        this.domainClass = ctx.args.domainClass
    }

    static SyncJobStartEvent of(SyncJobContext ctx) {
        return new SyncJobStartEvent(ctx)
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(domainClass))
    }
}
